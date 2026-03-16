package ch.sectioninformatique.template.config;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.context.i18n.LocaleContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

/**
 * Internationalization and localization configuration.
 */
@Configuration
public class LocaleConfig implements WebMvcConfigurer {

    // Constants for message source configuration and locale resolution.
    private static final String MESSAGE_RESOURCES_PATTERN = "classpath*:messages/**/*.properties";
    private static final String MESSAGE_SEGMENT = "/messages/";
    private static final String PROPERTIES_EXTENSION = ".properties";
    private static final String CLASSPATH_MESSAGES_PREFIX = "classpath:messages/";
    private static final String DEFAULT_MESSAGE_BASENAME = "classpath:messages/messages";
    private static final String REQUEST_LOCALE_ATTRIBUTE = "request.locale.override";

    private static final Pattern LOCALE_SUFFIX_PATTERN = Pattern.compile("_[a-z]{2}(?:_[A-Z]{2})?$");

    /**
     * Configures the MessageSource bean to load i18n message bundles from the classpath.
     * This method scans for all properties files under the "messages" directory and its subdirectories,
     * normalizes their paths to basenames, and sets up a ReloadableResourceBundleMessageSource with UTF-8 encoding and caching.
     * 
     * @return MessageSource configured for internationalization
     */
    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasenames(resolveMessageBasenames());
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setCacheSeconds(3600);
        return messageSource;
    }

    /**
     * Resolves message bundle basenames by scanning the classpath for properties files under the "messages" directory.
     * This method normalizes file paths to basenames by stripping locale suffixes and file extensions,
     * ensuring that all locale-specific bundles (e.g., messages_fr.properties) are treated as variants of a single basename (e.g., messages).
     * If no message bundles are found, it falls back to a default basename.
     * 
     * @return An array of basenames for the MessageSource configuration
     */
    private String[] resolveMessageBasenames() {
        try {
            Resource[] resources = new PathMatchingResourcePatternResolver().getResources(MESSAGE_RESOURCES_PATTERN);
            Set<String> basenames = new TreeSet<>();

            for (Resource resource : resources) {
                // Normalize message bundle paths to basenames (strip locale suffixes).
                String basename = resolveResourceBasename(resource);
                if (basename != null) {
                    basenames.add(basename);
                }
            }

            if (basenames.isEmpty()) {
                basenames.add(DEFAULT_MESSAGE_BASENAME);
            }

            return basenames.toArray(new String[0]);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to resolve i18n message bundles from classpath", exception);
        }
    }

    /**
     * Resolves the basename for a given resource by stripping locale suffixes and file extensions.
     * This ensures that all locale-specific bundles (e.g., messages_fr.properties) are treated as variants of a single basename (e.g., messages).
     *
     * @param resource The resource to resolve
     * @return The resolved basename, or null if the resource is not a valid message bundle
     * @throws IOException If an I/O error occurs while accessing the resource
     */
    private String resolveResourceBasename(Resource resource) throws IOException {
        String resourceUrl = resource.getURL().toString().replace('\\', '/');
        int messagesIndex = resourceUrl.lastIndexOf(MESSAGE_SEGMENT);
        if (messagesIndex < 0) {
            return null;
        }

        String relativePath = resourceUrl.substring(messagesIndex + MESSAGE_SEGMENT.length());
        if (!relativePath.endsWith(PROPERTIES_EXTENSION)) {
            return null;
        }

        String withoutExtension = relativePath.substring(0, relativePath.length() - PROPERTIES_EXTENSION.length());
        // Collapse locale-specific bundles (e.g., messages_fr) into a single basename.
        String basenameWithoutLocale = LOCALE_SUFFIX_PATTERN.matcher(withoutExtension).replaceFirst("");
        return CLASSPATH_MESSAGES_PREFIX + basenameWithoutLocale;
    }

    // ========================================================================
    // Locale Resolution and Interceptor Configuration
    // ========================================================================

    /**
     * Configures the LocaleResolver to determine the locale based on the "Accept-Language" header by default,
     * but allows overriding the locale via a request attribute set by the localeQueryParamInterceptor.
     * 
     * @return The configured LocaleResolver
     */
    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver localeResolver = new AcceptHeaderLocaleResolver() {
            @Override
            public Locale resolveLocale(HttpServletRequest request) {
                Object overriddenLocale = request.getAttribute(REQUEST_LOCALE_ATTRIBUTE);
                if (overriddenLocale instanceof Locale locale) {
                    return locale;
                }
                return super.resolveLocale(request);
            }

            @Override
            public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
                if (locale != null) {
                    request.setAttribute(REQUEST_LOCALE_ATTRIBUTE, locale);
                }
            }
        };
        localeResolver.setDefaultLocale(Locale.FRANCE);
        return localeResolver;
    }

    /**
     * Configures the LocalValidatorFactoryBean to use the MessageSource for resolving validation messages.
     * This allows validation annotations (e.g., @NotNull, @Size) to reference message keys defined in the i18n message bundles,
     * enabling localized validation error messages based on the resolved locale.
     * 
     * @param messageSource The MessageSource to be used for validation messages
     * @return The configured LocalValidatorFactoryBean
     */
    @Bean
    public LocalValidatorFactoryBean validator(MessageSource messageSource) {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.setValidationMessageSource(messageSource);
        return validator;
    }

    /**
     * Defines a HandlerInterceptor that checks for a "lang" query parameter in incoming requests to override the locale.
     * If the "lang" parameter is present and valid, it sets the locale for the current request and updates the LocaleContextHolder.
     * After the request is processed, it resets the LocaleContextHolder to prevent locale leakage across requests.
     * This allows clients to specify the desired locale on a per-request basis using a query parameter (e.g., ?lang=fr).
     * @return The configured HandlerInterceptor for locale resolution
     */
    @Bean
    public HandlerInterceptor localeQueryParamInterceptor() {
        HandlerInterceptor interceptor = new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
                String lang = request.getParameter("lang");
                if (lang != null && !lang.isBlank()) {
                    Locale locale = Locale.forLanguageTag(lang.replace('_', '-'));
                    request.setAttribute(REQUEST_LOCALE_ATTRIBUTE, locale);
                    LocaleContextHolder.setLocale(locale);
                }
                return true;
            }

            @Override
            public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                    Exception ex) {
                LocaleContextHolder.resetLocaleContext();
            }
        };
        return interceptor;
    }

    /**
     * Registers the localeQueryParamInterceptor to be applied to all incoming requests.
     * This ensures that the interceptor will check for the "lang" query parameter on every request and override the locale accordingly.
     * By adding this interceptor to the registry, we enable dynamic locale resolution based on client-specified query parameters, enhancing the internationalization capabilities of the application.
     * @param registry The InterceptorRegistry to which the localeQueryParamInterceptor will be added
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeQueryParamInterceptor());
    }
}
