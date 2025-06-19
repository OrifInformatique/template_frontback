import React, { useState } from "react";
import MultiCheckbox from "./MultiCheckbox";

export default {
  title: "Components/UI/MultiCheckbox",
  component: MultiCheckbox,
  tags: ["autodocs"],
  layout: "fullscreen"
};

export const Default = () => {
  const [selected, setSelected] = useState([]);

  const options = [
    { id: "cb1", name: "cb1", label: "Option 1" },
    { id: "cb2", name: "cb2", label: "Option 2", defaultChecked: true },
    { id: "cb3", name: "cb3", label: "Option 3", disabled: false },
    { id: "cb4", name: "cb4", label: "Option 4", disabled: false }
  ];

  return (
    <div className="p-4 space-y-4">
      <MultiCheckbox
        options={options}
        onChange={(checkedIds) => setSelected(checkedIds)}
      />

      <div className="text-sm text-gray-600">
        Sélectionnées: {selected.join(", ") || "Aucune"}
      </div>
    </div>
  );
};
