/**
 * Display name of an item's author.
 *
 * @param {{firstName?: string, lastName?: string}|null} author the author of the item,
 *        null once the author was permanently deleted
 * @param {Function} t translation function of the "items" namespace
 * @returns {string} "First Last", or a translated "unknown author"
 */
export const authorName = (author, t) =>
    author ? `${author.firstName ?? ''} ${author.lastName ?? ''}`.trim() : t("unknown_author", "Unknown author");
