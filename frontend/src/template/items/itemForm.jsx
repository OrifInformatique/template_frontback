import React, { useState } from "react";
import { useTranslation } from "react-i18next";
import { Button, InputText, Textarea,  } from '@orif-informatique/react-components-library';

import { createItem, modifyItem } from "./api/api";
import { apiErrorMessage } from "../auth/apiError";

const ItemForm = ({ item, onClose }) => {
    const { t } = useTranslation('items');

    const [name, setName] = useState(item ? item.name : "");
    const [description, setDescription] = useState(item ? item.description : "");
    const [saveError, setSaveError] = useState("");

    // Closes only once saved: on error, the form stays open with the message
    const handleSave = async () => {
        setSaveError("");
        try {
            if (item) {
                await modifyItem(item.id, { name, description });
            } else {
                await createItem({ name, description });
            }
            onClose();
        } catch (error) {
            console.error("Item save failed:", error);
            setSaveError(apiErrorMessage(error, t("save_error", "The item could not be saved.")));
        }
    };

    return (
        <>
            <InputText id="item-name" name="name" label={t("item_name", "Item Name")} value={name} onChangeFunction={(e) => setName(e.target.value)} />
            <Textarea id="item-description" name="description" label={t("item_description", "Item Description")} value={description} onChangeFunction={(e) => setDescription(e.target.value)} />
            {saveError ? <p className="mt-3 text-red-600" role="alert">{saveError}</p> : null}
            <div className="flex justify-end mt-4">
                <Button label={t("cancel", "Cancel")} variant="secondary" className="mr-2" onClick={() => {onClose()}} />
                <Button label={item ? t("save", "Save") : t("create", "Create")} variant="primary" onClick={handleSave} />
            </div>
        </>
    );
}

export default ItemForm;
