import React from "react";
import { useTranslation } from "react-i18next";

import { Button } from "@orif-informatique/react-components-library";
import useAuthStore from "../../auth/authStore";

/**
 * A generic list/table component that dynamically generates columns
 * from the keys of the provided items.
 *
 * @param {Object} props
 * @param {Array<Object>} props.items - The array of objects to display.
 * @param {Array<Object>} [props.actions] - Optional action buttons per row.
 *   Each action is { label: string, onClick: (item) => void, variant?: string }.
 * @param {Array<string>} [props.columns] - Optional subset/order of columns to display.
 *   If omitted, all keys from the first item are used.
 * @param {Object} [props.columnLabels] - Optional map of key -> display header label.
 * @param {boolean} [props.showDeleted] - Whether to show soft-deleted items.
 * @param {function} [props.onToggleShowDeleted] - Callback when the show-deleted checkbox changes.
 */
const List = ({ items = [], actions = [], columns, columnLabels = {}, showDeleted = false, onToggleShowDeleted }) => {
    const { t } = useTranslation("items");
    const hasPermission = useAuthStore((state) => state.hasPermission);

    if (!items.length) {
        return <p className="text-gray-500 italic">{t("no_items", "No items to display.")}</p>;
    }

    // Derive columns from the first item's keys if not explicitly provided
    const cols = columns ?? Object.keys(items[0]);

    // Only show the actions column if at least one action is visible to the user
    const visibleActions = actions.filter((action) => !action.permission || hasPermission(action.permission));
    const hasVisibleActions = visibleActions.length > 0;

    return (
        <div className="overflow-x-auto">
            {onToggleShowDeleted && (
                <label className="flex items-center gap-2 mb-4">
                    <input
                        type="checkbox"
                        checked={showDeleted}
                        onChange={(e) => onToggleShowDeleted(e.target.checked)}
                    />
                    {t("show_deleted", "Show deleted items")}
                </label>
            )}
            <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                    <tr>
                        {cols.map((col) => (
                            <th
                                key={col}
                                className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                            >
                                {columnLabels[col] ?? col}
                            </th>
                        ))}
                        {hasVisibleActions && (
                            <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                                {t("actions", "Actions")}
                            </th>
                        )}
                    </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                    {items.map((item, index) => (
                        <tr key={item.id ?? index} className="hover:bg-gray-50">
                            {cols.map((col) => (
                                <td key={col} className="px-6 py-4 text-sm text-gray-900">
                                    {String(item[col] ?? "")}
                                </td>
                            ))}
                            {hasVisibleActions && (
                                <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium align-middle">
                                    <div className="flex justify-end items-center gap-2">
                                        {visibleActions.map((action, actionIndex) => (
                                            <Button
                                                key={actionIndex}
                                                variant={action.variant || "primary"}
                                                label={action.label}
                                                icon={action.icon}
                                                size={action.size || "small"}
                                                hideTextOnMobile={action.hideTextOnMobile}
                                                onClick={() => action.onClick(item)}
                                            />
                                        ))}
                                    </div>
                                </td>
                            )}
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
};

export default List;
