import React, { useEffect, useState } from "react";

import Icon from "../icon/Icon";

/**
 * UI component to display images. If the image can't be displayed, a placeholder will show instead.
 *
 * @param {string} [src=null] Source of the image. Null by default.
 *
 * @param {string} [alt=null] Alternative text of the image. Null by default.
 *
 * @param {number} [size=300] The desired image size, in pixels. 300 by default.
 *
 * @returns {JSX.Elements}
 *
 */
const Image = ({
    src = null,
    alt = null,
    size = 300
}) =>
{
    if(src && !alt)
        console.warn("For accessibility reasons, it's preferable to add an alternative text to the image");

    const [insertImagePlaceholder, setInsertImagePlaceholder] = useState(false)

    /**
     * Tries to display the image when the source is updated.
     */
    useEffect(() =>
    {
        if(src)
            setInsertImagePlaceholder(false)

        else if(src === "")
            setInsertImagePlaceholder(true)
    }, [src])

    return (
        <>
            {insertImagePlaceholder ? (
                <div
                    className={"flex flex-wrap place-content-center bg-gray-400 rounded-md"}
                    style={{ width: `${size}px`, height: `${size}px` }}
                >
                    <Icon name="eye-slash" />
                </div>
            ) : (
                <img
                    src={src}
                    alt={alt}
                    style={{ width: `${size}px` }}
                    onError={() => setInsertImagePlaceholder(true)}
                    className={"h-auto"}
                />
            )}
        </>
    )
}

export default Image;