import React, { useState } from "react";
import { useTranslation } from "react-i18next";
import { Button, InputText, Textarea,  } from '@orif-informatique/react-components-library';

import { createItem, modifyItem } from "./api/api";

const ItemDetail = ({ item, onClose }) => {
    const { t } = useTranslation('items');

    return (
        <>
            
            <div className="flex justify-end mt-4">
                <Button label={t("close", "Close")} variant="secondary" className="mr-2" onClick={() => {onClose()}} />
            </div>
        </>
    );
}

export default ItemDetail;