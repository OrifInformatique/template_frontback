import React from 'react';
import { useTranslation } from 'react-i18next';
import Title from '../../common/ui/title';
import UserList from '../users';
import RequireRole from '../../common/utils/requireRole';

const Admin = () => {
    const { t } = useTranslation("admin", "common");

    return (
        <RequireRole role="ADMIN">
                <Title>{t("admin_title")}</Title>
                <UserList />
        </RequireRole>
    );
}

export default Admin;