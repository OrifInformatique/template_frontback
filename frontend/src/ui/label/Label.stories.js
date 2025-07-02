import React from "react";
import Label from "./Label";

export default {
  title: "Components/UI/Label",
  component: Label,
  tags: ["autodocs"],
  parameters: {
    layout: "fullscreen",
  },
};

export const Required = () => (
  <Label htmlFor="file" required>
    <Label.Title>Label</Label.Title>
  </Label>
);

export const NotRequired = () => (
  <Label htmlFor="file">
    <Label.Title>Label</Label.Title>
  </Label>
);

