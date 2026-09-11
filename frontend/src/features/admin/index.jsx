import React from 'react';
import { useTranslation } from 'react-i18next';
import Title from '../../common/ui/title';
import UserList from '../users';
import RequireRole from '../../common/utils/requireRole';
import { NavBar } from '@orif-informatique/react-components-library';
import { Outlet } from 'react-router-dom';

const Admin = () => {
    const { t } = useTranslation("admin", "common");

    return (
        <RequireRole role="ADMIN">
                <Title>{t("admin_title")}</Title>
                <NavBar
                    links={[
                        { label: t("users"), to: "/admin/users" },
                        // You can add more links here for other admin pages, add them in the routes inside index.js in the admin route
                    ]}
                    activeLinkClassName="border-b-2 border-primary text-primary"
                    linksAlign="center"
                    className="w-full"
                    listClassName="md:w-full md:justify-center md:!flex-row"
                    burgerPosition="left"
                />
                <Outlet />
        </RequireRole>
    );
}

export default Admin;