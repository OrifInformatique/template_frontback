/**
 * ReportBar.jsx
 *
 * Reusable progress bar component built div elements and css property "flex-basis".
 * - Automatically normalizes incoming data.
 * - Fully responsive thanks to CSS container and the component's own settings.
 */

import React from "react";
import "./ReportBar.css";

// Default order of labels
const CATEGORY_ORDER = ["Autonome", "Exercé", "Expliqué", "Non expliqué"];

/**
 * Ensures the incoming dataset is always valid.
 * If no data is provided, a progress bar with a gray background will be displayed.
 */

function normalizeData(data) {
  if (!Array.isArray(data) || data.lenght === 0) {
    return [
      {
        label: "No data or incorrect data",
        value: 100,
        color: "#E0E0E0",
        isEmpty: true,
      },
    ];
  }
}

/**
 * Main ReportBar component
 */
export default function ReportBar() {
  return <ReportBar className="reportbar-style" />;
}
