import InputFile from "./InputFile";

export default {
    title: "Components/UI/InputFile",
    component: InputFile,
    tags: ["autodocs"],
    layout: "fullscreen"
}

export const WithLabel = {
    args: {
        id: "file-1",
        name: "file-1",
        label: "Sélectionnez un fichier :",
        required: true
    }
}

export const Disabled = {
    args: {
        id: "file-2",
        name: "file-2",
        disabled: true,
    }
}

export const RestrictedFileTypes = {
    args: {
        id: "file-3",
        name: "file-3",
        label: "Sélectionnez un fichier (.docx, .pdf) :",
        accept: ".docx, .pdf",
        required: false
    }
}