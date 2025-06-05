import SnackBar from "./SnackBar";

export default {
    title: "Components/UI/SnackBar",
    component: SnackBar,
    tags: ["autodocs"],
    layout: "fullscreen",
    args: {
        message: "This is a snackbar message."
    }
}

export const Default = {
    args: {
        type: "neutral",
        autoCloseTimer: 5000
    }
}

export const Info = {
    args: {
        type: "info"
    }
}

export const Success = {
    args: {
        type: "success"
    }
}

export const Warning = {
    args: {
        type: "warning"
    }
}

export const Error = {
    args: {
        type: "error"
    }
}

export const ShortTimer = {
    args: {
        autoCloseTimer: 100
    }
}

export const VeryLongMessage = {
    args: {
        message: "This is a ridiculously, unnecessarily, outrageously, catastrophically, unnecessarily verbose, grandiloquent, extravagantly bloated, ultra-chunky, mega-monolithic snackbar message carefully engineered to push the boundaries of UI rendering and to make frontend developers weep softly in a dark corner.",
        autoCloseTimer: 60000
    }
}

export const VeryLongMessageWithIcon = {
    args: {
        type: "warning",
        message: "Warning: This excessively long, super massive, ultra thicc, intergalactically verbose snackbar message may cause irreversible psychological damage to your layout engine, melt your flexbox, and awaken long-buried CSS traumas.",
        autoCloseTimer: 60000
    }
}