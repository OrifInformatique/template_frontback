import React from 'react';

import { Button, PopUp } from '@orif-informatique/react-components-library';
import useAuthStore from '../../auth/authStore';

/**
 * A generic list/table component that dynamically generates columns
 * from the keys of the provided items.
 *
 * @param {Object} props
 * @param {Array<Object>} props.items - The array of objects to display.
 * @param {Object} [props.actions] - Optional action config keyed by action name.
 *   Each action is { permission?: string, onClick?: (item) => void }.
 *   Supported keys: edit, delete, restore, hardDelete, viewDeleted, view.
 * @param {function} [props.hasPermission] - Permission check function (permission) => boolean.
 * @param {Array<string>} [props.columns] - Optional subset/order of columns to display.
 *   If omitted, all keys from the first item are used.
 * @param {Object} [props.columnLabels] - Optional map of key -> display header label.
 * @param {boolean} [props.showDeleted] - Whether to show soft-deleted items.
 * @param {function} [props.onToggleShowDeleted] - Callback when the show-deleted checkbox changes.
 * @param {string} [props.actionsLabel] - Optional label for the actions column.
 * @param {string} [props.showDeletedLabel] - Optional label for the show deleted checkbox.
 * @param {string} [props.noItemsLabel] - Optional label for when there are no items.
 */
const List = ({
    items = [],
    actions = {},
    columns,
    columnLabels = {},
    showDeleted = false,
    onToggleShowDeleted,
    actionsLabel,
    showDeletedLabel,
    noItemsLabel,
    confirmHardDeleteLabel,
    confirmHardDeleteLabelText,
}) => {
    const [hardDeleteTarget, setHardDeleteTarget] = React.useState(null);
    const hasPermission = useAuthStore((state) => state.hasPermission);

    // Derive columns from the first item's keys if not explicitly provided
    const cols = columns ?? (items.length ? Object.keys(items[0]) : []);

    // Check permissions for each action
    const canEdit =
        actions.edit &&
        (!actions.edit.permission || hasPermission(actions.edit.permission));
    const canDelete =
        actions.delete &&
        (!actions.delete.permission ||
            hasPermission(actions.delete.permission));
    const canRestore =
        actions.restore &&
        (!actions.restore.permission ||
            hasPermission(actions.restore.permission));
    const canHardDelete =
        actions.hardDelete &&
        (!actions.hardDelete.permission ||
            hasPermission(actions.hardDelete.permission));
    const canViewDeleted =
        actions.viewDeleted &&
        (!actions.viewDeleted.permission ||
            hasPermission(actions.viewDeleted.permission));
    const hasVisibleActions =
        canEdit || canDelete || canRestore || canHardDelete;

    return (
        <div className="overflow-x-auto">
            {onToggleShowDeleted && canViewDeleted && (
                <label className="flex items-center justify-end gap-2 mb-4 mr-4">
                    {showDeletedLabel ?? 'Show deleted items'}
                    <input
                        type="checkbox"
                        checked={showDeleted}
                        onChange={(e) => onToggleShowDeleted(e.target.checked)}
                    />
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
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                {actionsLabel ?? ''}
                            </th>
                        )}
                    </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                    {items.length === 0 ?
                        <tr>
                            <td
                                colSpan={
                                    cols.length + (hasVisibleActions ? 1 : 0)
                                }
                                className="px-6 py-4 text-sm text-gray-500 italic text-center"
                            >
                                {noItemsLabel ?? 'No items to display.'}
                            </td>
                        </tr>
                    :   items.map((item, index) => (
                            <tr
                                key={item.id ?? index}
                                className="hover:bg-gray-50"
                            >
                                {cols.map((col) => (
                                    <td
                                        key={col}
                                        className={`px-6 py-4 text-sm text-gray-900 ${item.isDeleted ? 'line-through text-gray-400' : ''} ${canView ? 'cursor-pointer hover:underline' : ''}`}
                                        onClick={canView ? () => actions.view.onClick(item) : undefined}
                                    >
                                        {String(item[col] ?? '')}
                                    </td>
                                ))}
                                {hasVisibleActions && (
                                    <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium align-middle">
                                        <div className="flex justify-end items-center gap-2">
                                            {canEdit && !item.isDeleted && (
                                                <Button
                                                    variant="secondary"
                                                    icon="edit"
                                                    onClick={() =>
                                                        actions.edit.onClick(
                                                            item,
                                                        )
                                                    }
                                                />
                                            )}
                                            {canDelete && !item.isDeleted && (
                                                <Button
                                                    variant="secondary"
                                                    icon="delete"
                                                    onClick={() =>
                                                        actions.delete.onClick(
                                                            item,
                                                        )
                                                    }
                                                />
                                            )}
                                            {canRestore && item.isDeleted && (
                                                <Button
                                                    variant="secondary"
                                                    icon="restore"
                                                    onClick={() =>
                                                        actions.restore.onClick(
                                                            item,
                                                        )
                                                    }
                                                />
                                            )}
                                            {canHardDelete &&
                                                item.isDeleted && (
                                                    <>
                                                        <Button
                                                            variant="danger"
                                                            icon="delete"
                                                            onClick={() =>
                                                                setHardDeleteTarget(
                                                                    item,
                                                                )
                                                            }
                                                        />
                                                        {hardDeleteTarget &&
                                                            hardDeleteTarget.id ===
                                                                item.id && (
                                                                <PopUp
                                                                    title={
                                                                        confirmHardDeleteLabel
                                                                    }
                                                                    onClose={() =>
                                                                        setHardDeleteTarget(
                                                                            null,
                                                                        )
                                                                    }
                                                                >
                                                                    <p className="whitespace-normal break-words text-left">
                                                                        {
                                                                            confirmHardDeleteLabelText
                                                                        }
                                                                    </p>
                                                                    <div className="flex justify-end gap-2 mt-4">
                                                                        <Button
                                                                            variant="secondary"
                                                                            icon="restore"
                                                                            onClick={() =>
                                                                                setHardDeleteTarget(
                                                                                    null,
                                                                                )
                                                                            }
                                                                        />
                                                                        <Button
                                                                            variant="danger"
                                                                            icon="delete"
                                                                            onClick={() => {
                                                                                actions.hardDelete.onClick(
                                                                                    item,
                                                                                );
                                                                                setHardDeleteTarget(
                                                                                    null,
                                                                                );
                                                                            }}
                                                                        />
                                                                    </div>
                                                                </PopUp>
                                                            )}
                                                    </>
                                                )}
                                        </div>
                                    </td>
                                )}
                            </tr>
                        ))
                    }
                </tbody>
            </table>
        </div>
    );
};

export default List;
