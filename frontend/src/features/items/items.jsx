import React, { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';

import { getItems } from './api/api';
import List from './ui/list';

const Items = () => {
    const { t } = useTranslation('items');
    const [items, setItems] = useState([]);

    useEffect(() => {
        const fetchItems = async () => {
            const data = await getItems();
            setItems(data);
        };

        fetchItems();
    }, []);

    // Example actions — adapt these to your needs
    const actions = [
        {
            label: t("edit", "Edit"),
            permission: "user:update", // Optional permission required to see this action
            onClick: (item) => console.log("Edit", item),
        },
        {
            label: t("delete", "Delete"),
            permission: "user:delete", // Optional permission required to see this action
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
            />
        </div>
    );
};

export default Items;
