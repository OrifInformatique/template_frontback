import React, { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';

import { Icon } from "@orif-informatique/react-components-library";

import { getItems } from './api/api';
import List from './ui/list';

const Items = () => {
    const { t } = useTranslation('items');
    const [items, setItems] = useState([]);
    const [showDeleted, setShowDeleted] = useState(false);

    useEffect(() => {
        const fetchItems = async () => {
            const data = await getItems(showDeleted);
            setItems(data);
        };

        fetchItems();
    }, [showDeleted]);

    // Example actions — adapt these to your needs
    const actions = [
        {
            // label: t("edit", "Edit"),
            icon: "edit",
            permission: "user:update", // permission required to see this action
            onClick: (item) => console.log("Edit", item),
        },
        {
            // label: t("delete", "Delete"),
            icon: "delete",
            permission: "user:delete", // permission required to see this action
            onClick: (item) => console.log("Delete", item),
        },
    ];

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
