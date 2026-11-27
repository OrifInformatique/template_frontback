import React, { useMemo, useCallback, useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';

import { getItems, modifyItem, deleteItem, restoreItem, hardDeleteItem } from './api/api';
import ItemForm from './itemForm';
import ItemDetail from './itemDetail';
import { apiErrorMessage } from '../auth/apiError';
import { Button, PopUp, List } from '@orif-informatique/react-components-library';
import useAuthStore from '../auth/authStore';

const Items = () => {
    const { t } = useTranslation('items');
    const hasPermission = useAuthStore((state) => state.hasPermission);
    const [items, setItems] = useState([]);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState(null);
    const [showDeleted, setShowDeleted] = useState(false);
    const [formOpen, setFormOpen] = useState(false);
    const [selectedItem, setSelectedItem] = useState(null);
    const [itemOpen, setItemOpen] = useState(false);

    // Fetch items on mount and when showDeleted changes.
    // Cleanup ignores stale responses to prevent race conditions.
    useEffect(() => {
        let ignore = false;
        setIsLoading(true);
        setError(null);
        getItems(showDeleted ? 'ALL' : 'ACTIVE')
            .then((data) => {
                if (!ignore) setItems(data);
            })
            .catch((err) => {
                if (!ignore) setError(apiErrorMessage(err, t("fetch_error", "Failed to load items.")));
            })
            .finally(() => {
                if (!ignore) setIsLoading(false);
            });
        return () => { ignore = true; };
    }, [showDeleted]);

    // Imperative refresh for event handlers (after mutations).
    const fetchItems = useCallback(async () => {
        setIsLoading(true);
        setError(null);
        try {
            const data = await getItems(showDeleted ? 'ALL' : 'ACTIVE');
            setItems(data);
        } catch (err) {
            setError(apiErrorMessage(err, t("fetch_error", "Failed to load items.")));
        } finally {
            setIsLoading(false);
        }
    }, [showDeleted, t]);

    useEffect(() => {
        fetchItems();
    }, [fetchItems]);

    // Runs a list action, then reloads the list; shows the error if it fails
    // (e.g. 403 when the user is not the author of the item)
    const runAction = useCallback(async (action) => {
        try {
            await action();
            await fetchItems();
        } catch (err) {
            console.error("Item action failed:", err);
            setError(apiErrorMessage(err, t("action_error", "The action failed.")));
        }
    }, [fetchItems, t]);

    // The list shows items formatted for display (dates, author name): the
    // detail view and the form get the original item
    const originalItem = useCallback((row) => items.find((item) => item.id === row.id) ?? row, [items]);

    const actions = useMemo(() => ({
        // Clicking a row opens its detail
        view: { permission: "item:read", onClick: (row) => { setSelectedItem(originalItem(row)); setItemOpen(true); } },
        edit: { permission: "item:update", onClick: (row) => { setSelectedItem(originalItem(row)); setFormOpen(true); } },
        delete: { permission: "item:delete", onClick: (item) => runAction(() => deleteItem(item.id)) },
        restore: { permission: "item:update", onClick: (item) => runAction(() => restoreItem(item.id)) },
        hardDelete: { permission: "item:delete", onClick: (item) => runAction(() => hardDeleteItem(item.id)) },
        viewDeleted: { permission: "item:read" },
    }), [runAction, originalItem]);

    return (
        <div>
            {formOpen ? (
                <PopUp
                    onClose={() => setFormOpen(false)}
                    title={selectedItem ? t("edit_item", "Edit Item") : t("create_item", "Create Item")}
                    children={<ItemForm item={selectedItem} onClose={() => { setFormOpen(false); fetchItems(); }} />}
                />
            ) : null}
            {itemOpen ? (
                <PopUp
                    onClose={() => setItemOpen(false)}
                    title={t("item_details", "Item Details")}
                    children={<ItemDetail item={selectedItem} onClose={() => { setItemOpen(false); }} />}
                />
            ) : null
                    }

            {isLoading && <p className="text-center text-gray-500 py-4">{t("loading", "Loading...")}</p>}
            {error && <p className="text-center text-red-500 py-4">{error}</p>}
            {hasPermission("item:write") && <Button label={t("create_item", "Create Item")} variant="primary" className="mb-4" onClick={() => { setSelectedItem(null); setFormOpen(true); }} />}
            <List
                items={items.map((item) => ({
                    ...item,
                    author: item.author ? `${item.author.firstName ?? ''} ${item.author.lastName ?? ''}`.trim() : t("unknown_author", "Unknown author"),
                    createdAt: new Date(item.createdAt).toLocaleString(),
                    updatedAt: new Date(item.updatedAt).toLocaleString(),
                }))}
                columns={["id", "name", "author", "description", "createdAt", "updatedAt"]}
                columnsLabels={{
                    id: "#",
                    name: t("name", "Name"),
                    author: t("author", "Author"),
                    description: t("description", "Description"),
                    createdAt: t("createdAt", "Created At"),
                    updatedAt: t("updatedAt", "Updated At"),
                }}
                actions={actions}
                hasPermission={hasPermission}
                actionsLabel={t("actions", "Actions")}
                showDeletedLabel={t("show_deleted", "Show deleted items")}
                noItemsLabel={t("no_items", "No items to display.")}
                showDeleted={showDeleted}
                onToggleShowDeleted={setShowDeleted}
                isDeletedKey = 'deleted'
                confirmHardDeleteTitle={t("confirm_hard_delete", "Confirm Permanent Deletion")}
                confirmHardDeleteText={t("confirm_hard_delete_text", "Are you sure you want to permanently delete this item? This action cannot be undone.")}
            />
        </div>
    );
};

export default Items;
