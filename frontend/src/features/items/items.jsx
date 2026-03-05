import React, { useCallback, useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';

import { getItems, modifyItem, deleteItem, restoreItem, hardDeleteItem } from './api/api';
import List from './ui/list';

const Items = () => {
    const { t } = useTranslation('items');
    const [items, setItems] = useState([]);
    const [showDeleted, setShowDeleted] = useState(false);

    const fetchItems = useCallback(async () => {
        const data = await getItems(showDeleted);
        setItems(data);
    }, [showDeleted]);

    useEffect(() => {
        fetchItems();
    }, [fetchItems]);

    const actions = {
        // TODO: Replace hardcoded edit with a proper edit form/modal
        edit: { permission: "user:update", onClick: (item) => modifyItem(item.id, { name: item.name + " (edited)" }).then(() => fetchItems()).catch((err) => console.error("Edit failed:", err)) },
        delete: { permission: "user:delete", onClick: (item) => deleteItem(item.id).then(() => fetchItems()).catch((err) => console.error("Delete failed:", err)) },
        restore: { permission: "user:write", onClick: (item) => restoreItem(item.id).then(() => fetchItems()).catch((err) => console.error("Restore failed:", err)) },
        hardDelete: { permission: "user:delete", onClick: (item) => hardDeleteItem(item.id).then(() => fetchItems()).catch((err) => console.error("Hard delete failed:", err)) },
        viewDeleted: { permission: "user:read" },
    };

    return (
        <div>
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
            />
        </div>
    );
};

export default Items;
