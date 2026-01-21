import i18n from 'i18next';
import Backend from 'i18next-http-backend';
import LanguageDetector from 'i18next-browser-languagedetector';
import { initReactI18next } from 'react-i18next';

i18n
    .use(Backend)
    .use(LanguageDetector)
    .use(initReactI18next)
    .init(
    {
        debug: false,

        supportedLngs: ["fr", "en"],
        fallbackLng: 'fr',

        detection:
        {
            // Always let query param win and avoid sticky cached language
            order: ['querystring', 'navigator', 'htmlTag', 'cookie', 'localStorage'],
            caches: [],
            lookupQuerystring: 'lng'
        },

        backend:
        {
            // Avoid protocol-relative URL when APP_ROOT is '/'
            loadPath: `${process.env.APP_ROOT && process.env.APP_ROOT !== "/" ? process.env.APP_ROOT : ""}/locales/{{lng}}/{{ns}}.json`
        },

        ns: ["auth", "contact", "home", "common"],

        interpolation:
        {
            escapeValue: false
        },

        react:
        {
            useSuspense: true
        }
    });

export default i18n;