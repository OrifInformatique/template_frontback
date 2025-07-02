import React, { useState, useEffect } from "react";

import clsx from "clsx";

import Button from "../../buttons/default/Button";
import Image from "../../image/Image";
import Label from "../../label/Label";

import { formatBytes } from "../../../utils/fileUtils";

/**
 * UI component to display a image upload input.
 *
 * @param {string} name Name of the image upload input. Required.
 *
 * @param {number} [imagePreviewSize=300] Size of the image preview. 300 by default.
 *
 * @param {string[]} [errors=[]] Invalid value errors for this field. Empty array by default.
 *
 * @param {string} [className=null] Additional and specific styles for the image upload input. Null by default.
 *
 * @returns {JSX.Element}
 *
 */
const InputFileImage = ({
    name,
    imagePreviewSize = 300,
    className = null
}) =>
{
    if(!name)
    {
        console.error("InputFileImage must have a name.");
        return;
    }

    const allowedFileTypes = ["image/png", "image/jpg", "image/jpeg"];

    const [isImageUploaded, setIsImageUploaded] = useState(false);
    const [imagePreviewSrc, setImagePreviewSrc] = useState("");
    const [imageName, setImageName] = useState("");
    const [imageSize, setImageSize] = useState(0);

    /**
     * Handles image upload and updates the preview.
     *
     * Validates the selected file, ensures it matches allowed types, and updates
     * the preview source. Deletes any previously uploaded image if no file is selected.
     *
     * @param {Event} event The file input change event.
     *
     * @returns {void}
     *
     */
    const handleFileUpload = (event) =>
    {
        const image = event.target.files[0]

        if(!image)
        {
            deleteUploadedFile();
            return;
        }

        if(!allowedFileTypes.includes(image.type))
        {
            console.error("Invalid file type: ", image.type);
            deleteUploadedFile();
            return;
        }

        setImagePreviewSrc(URL.createObjectURL(image));

        setImageName(image.name);
        setImageSize(formatBytes(image.size));
        setIsImageUploaded(true);
    }

    /**
     * Deletes the image preview.
     *
     * @returns {void}
     *
     */
    const deleteUploadedFile = () =>
    {
        document.getElementById(name).value = "";

        setImagePreviewSrc("");
        setImageName("");
        setImageSize(0);
        setIsImageUploaded(false);

        URL.revokeObjectURL(imagePreviewSrc);
    }

    /**
     * Deletes the Blob object on unmount to prevent memory issues.
     */
    useEffect(() =>
    {
        return () =>
        {
            if(isImageUploaded)
                URL.revokeObjectURL(imagePreviewSrc)
        }
    }, [imagePreviewSrc])

    return (
        <>
            <div className={className}>
                <div
                    onClick={() => document.getElementById(name).click()}
                    className="relative flex justify-center hover:cursor-pointer"
                >
                    <Image
                        src={imagePreviewSrc}
                        size={imagePreviewSize}
                    />

                    <Label
                        forInput={name}
                        className={clsx(
                            "absolute bottom-0 py-2 rounded-b-md bg-black/50 text-white text-center",
                            `w-[${imagePreviewSize}px]`
                        )}
                    >
                        Ajouter ou modifier une image
                    </Label>
                </div>

                {isImageUploaded &&
                    <>
                        <p className="flex justify-between">
                            <span>{imageName}</span>

                            <span>{imageSize}</span>
                        </p>

                        <Button
                            variant="primary"
                            label="Supprimer l'image"
                            onClick={deleteUploadedFile}
                            className={"mt-2 h-fit"}
                        />
                    </>
                }

                <input
                    type="file"
                    id={name}
                    name={name}
                    accept={allowedFileTypes.join(",")}
                    onChange={handleFileUpload}
                    className={"hidden"}
                />
            </div>
        </>
    )
}

export default InputFileImage;