import React from "react";
import DoughnutChart from "../doughnut-chart/DoughnutChart";
import ReportLine from "../report-line/ReportLine";

/**
 * ReportSection component
 *
 * Parent component that contains :
 *  - a section label
 *  - a DoughnutChart component
 *  - several ReportLine components
 *
 * How this component is displayed:
 *
 * - On large screens:
 *     The label spans the full width at the top.
 *     The DoughnutChart is positioned on the left.
 *     ReportLine components are displayed on the right.
 *
 * - On small screens:
 *     The label spans the full width at the top.
 *     The DoughnutChart is positioned on the left.
 *     ReportLine components are hidden and replaced by a toggle button on the right,
 *     which reveals the detailed ReportLines in a card layout when clicked.
 *
 * @param {Object} props
 * @param {string} props.label             - Section title received from the database.
 * @param {Array}  props.doughnutChartData - Data passed to DoughnutChart.
 * @param {Array}  props.reportLines       - Sets of Data for several ReportLine.
 */

export default function ReportSection({
  label = "",
  doughnutChartData = [],
  reportLines = [],
}) {
  return (
    <div className="report-section">
      <div className="report-section__title" title={label}>
        {label}
      </div>
      <div className="report-section__doughnut-wrapper">
        <DoughnutChart data={doughnutChartData} />
      </div>
      <div className="report-section__lines-wrapper">
        {reportLines.map((reportLine) => (
          <ReportLine
            key={reportLine.label}
            label={reportLine.label}
            data={reportLine.data}
          />
        ))}
      </div>
    </div>
  );
}
