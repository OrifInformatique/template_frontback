// Plugin to display percentages in each segment
export const percentagePlugin = {
  id: "percentagePlugin",

  // AfterDatasetDraw is a hook provided by Chart.js to add custom drawing to the canvas
  afterDatasetDraw(chart, args, options) {
    // Const ctx = chart.ctx; 2D context of the <canvas>
    const { ctx } = chart;
    const meta = chart.getDatasetMeta(0);
    const dataset = chart.data.datasets[0];
    if (!meta || !dataset || !meta.data || meta.data.length === 0) return;

    // Sum of the values in the dataset
    const total = dataset.data.reduce((sum, v) => sum + (Number(v) || 0), 0);
    if (!total) return;

    // Retrieving the size of the donut from the first arc
    const firstArc = meta.data[0];
    if (
      !firstArc ||
      typeof firstArc.innerRadius !== "number" ||
      typeof firstArc.outerRadius !== "number"
    ) {
      return;
    }
    const { innerRadius, outerRadius } = firstArc;
    const ringThickness = outerRadius - innerRadius;

    const {
      bubbleScale = 0.6,
      maxWidth = 120,
      maxHeight = 80,
      minPercentage = 3, // minimum value required to draw a segment
    } = options || {};

    const bubbleRadius = bubbleScale * ringThickness;
    // Font size proportional to the bubble size
    const fontSize = Math.max(10, Math.round(bubbleRadius * 0.5)); // min 10px

    // Calculate the % and return the segment’s visual center
    meta.data.forEach((arc, i) => {
      const raw = dataset.data[i];
      const value = Number(raw) || 0;
      if (!value) return;

      const pct = total ? Math.round((value / total) * 100) : 0;

      if (pct < minPercentage) return;

      const pos = arc.tooltipPosition();

      // Invoke the functions to render the bubble and its text
      drawBlurBubble(ctx, pos.x, pos.y, bubbleRadius, maxWidth, maxHeight);
      drawPercentageText(ctx, pos.x, pos.y, pct, fontSize);
    });
  },
};

// Function to draw a float value bubble
function drawBlurBubble(ctx, x, y, radius, maxWidth, maxHeight) {
  const width = Math.min(radius * 2.2, maxWidth);
  const height = Math.min(radius * 1.6, maxHeight);

  const r = Math.min(radius, width / 2, height / 2);

  const left = x - width / 2;
  const top = y - height / 2;
  const right = left + width;
  const bottom = top + height;

  ctx.save();
  ctx.fillStyle = "rgba(255,255,255,0.8)";
  ctx.filter = "blur(5px)";
  ctx.beginPath();
  ctx.moveTo(left + r, top);
  ctx.lineTo(right - r, top);
  ctx.quadraticCurveTo(right, top, right, top + r);
  ctx.lineTo(right, bottom - r);
  ctx.quadraticCurveTo(right, bottom, right - r, bottom);
  ctx.lineTo(left + r, bottom);
  ctx.quadraticCurveTo(left, bottom, left, bottom - r);
  ctx.lineTo(left, top + r);
  ctx.quadraticCurveTo(left, top, left + r, top);
  ctx.closePath();

  ctx.fill();
  ctx.restore();
}

// Function to render text inside the bubble
function drawPercentageText(ctx, x, y, pct, fontSize) {
  ctx.save();
  ctx.fillStyle = "#005BA9";
  ctx.font = `bold ${fontSize}px sans-serif`;
  ctx.textAlign = "center";
  ctx.textBaseline = "middle";
  ctx.fillText(`${pct}%`, x, y);
  ctx.restore();
}
