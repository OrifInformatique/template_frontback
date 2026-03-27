import React, { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';

import { getItems, deleteItem, restoreItem, hardDeleteItem } from './api/api';
import List from './ui/list';
import ItemForm from './itemForm';
import ItemDetail from './itemDetail';
import { Button, PopUp } from '@orif-informatique/react-components-library';

const Items = () => {
    const { t } = useTranslation('items');
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
        getItems(showDeleted)
            .then((data) => {
                if (!ignore) setItems(data);
            })
            .catch((err) => {
                if (!ignore) setError(err.message || t("fetch_error", "Failed to load items."));
            })
            .finally(() => {
                if (!ignore) setIsLoading(false);
            });
        return () => { ignore = true; };
    }, [showDeleted]);

    // Imperative refresh for event handlers (after mutations).
    const fetchItems = async () => {
        setIsLoading(true);
        setError(null);
        try {
            const data = await getItems(showDeleted);
            setItems(data);
        } catch (err) {
            setError(err.message || t("fetch_error", "Failed to load items."));
        } finally {
            setIsLoading(false);
        }
    };

    const actions = {
        edit: { permission: "user:update", onClick: (item) => { setSelectedItem(item); setFormOpen(true); } },
        delete: { permission: "user:delete", onClick: (item) => deleteItem(item.id).then(() => fetchItems()).catch((err) => console.error("Delete failed:", err)) },
        restore: { permission: "user:write", onClick: (item) => restoreItem(item.id).then(() => fetchItems()).catch((err) => console.error("Restore failed:", err)) },
        hardDelete: { permission: "user:delete", onClick: (item) => hardDeleteItem(item.id).then(() => fetchItems()).catch((err) => console.error("Hard delete failed:", err)) },
        viewDeleted: { permission: "user:read" },
        view: { permission: "user:read", onClick: (item) => { setSelectedItem(item); setItemOpen(true); } },
    };

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
            <Button label={t("create_item", "Create Item")} variant="primary" className="mb-4" onClick={() => { setSelectedItem(null); setFormOpen(true); }} />
            <List
                items={items}
                columns={["id", "name", "author", "description", "createdAt", "updatedAt"]}
                columnLabels={{
                    id: "#",
                    name: t("name", "Name"),
                    author: t("author", "Author"),
                    description: t("description", "Description"),
                    createdAt: t("createdAt", "Created At"),
                    updatedAt: t("updatedAt", "Updated At"),
                }}
                actions={actions}
                actionsLabel={t("actions", "Actions")}
                showDeletedLabel={t("show_deleted", "Show deleted items")}
                noItemsLabel={t("no_items", "No items to display.")}
                showDeleted={showDeleted}
                onToggleShowDeleted={setShowDeleted}
                confirmHardDeleteLabel={t("confirm_hard_delete", "Confirm Permanent Deletion")}
                confirmHardDeleteLabelText={t("confirm_hard_delete_text", "Are you sure you want to permanently delete this item? This action cannot be undone.")}
            />
        </div>
    );
};

export default Items;
