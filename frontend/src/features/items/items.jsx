import React, { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';

import { Icon } from "@orif-informatique/react-components-library";

import { getItems, modifyItem, deleteItem, restoreItem, hardDeleteItem } from './api/api';
import List from './ui/list';

const Items = () => {
    const { t } = useTranslation('items');
    const [items, setItems] = useState([]);
    const [showDeleted, setShowDeleted] = useState(false);

    const fetchItems = async () => {
        const data = await getItems(showDeleted);
        setItems(data);
    };

    useEffect(() => {
        fetchItems();
    }, [showDeleted]);

    const actions = {
        edit: { permission: "user:update", onClick: (item) => modifyItem(item.id, { name: item.name + " (edited)" }).then(() => fetchItems()) },
        delete: { permission: "user:delete", onClick: (item) => deleteItem(item.id).then(() => fetchItems()) },
        restore: { permission: "user:write", onClick: (item) => restoreItem(item.id).then(() => fetchItems()) },
        hardDelete: { permission: "user:delete", onClick: (item) => hardDeleteItem(item.id).then(() => fetchItems()) },
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
                showDeleted={showDeleted}
                onToggleShowDeleted={setShowDeleted}
            />
        </div>
    );
};

export default Items;
