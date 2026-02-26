import React, { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';

import { Button } from '@orif-informatique/react-components-library';
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
            onClick: (item) => console.log("Edit", item),
        },
        {
            label: t("delete", "Delete"),
            onClick: (item) => console.log("Delete", item),
        },
    ];

    return (
        <div>
            <List
                items={items}
                columns={["id", "name", "description", "date"]}
                columnLabels={{
                    id: "#",
                    name: t("name", "Name"),
                    description: t("description", "Description"),
                    date: t("date", "Date"),
                }}
                actions={actions}
            />
        </div>
    );
};

export default Items;
