import { useEffect, useState } from "react";
import clsx from "clsx";

import Icon from "../icon/Icon";

const Snackbar = ({
    type = "neutral",
    message,
    autoCloseTimer = 5000,
    className
}) =>
{
    const snackBarTypes =
    {
        "neutral": {
            icon: null,
            backgroundColor: "bg-white",
            borderColor: "border-black",
            color: "text-black"
        },
        "info": {
            icon: "info",
            backgroundColor: "bg-primary",
            borderColor: "border-black",
            color: "text-white"
        },
        "success": {
            icon: "check",
            backgroundColor: "bg-green-500/80",
            borderColor: "border-black",
            color: "text-white"
        },
        "warning": {
            icon: "home", // TODO : Add missing warning icon
            backgroundColor: "bg-amber-500/80",
            borderColor: "border-black",
            color: "text-white"
        },
        "error": {
            icon: "home", // TODO : Add missing error icon
            backgroundColor: "bg-red-500/80",
            borderColor: "border-black",
            color: "text-white"
        }
    }

    const [isDisplayed, setIsDisplayed] = useState(true);
    const [progress, setProgress] = useState(100);
    const intervalDelay = 100;

    useEffect(() => {
        const start = Date.now();

        const interval = setInterval(() => {
            const elapsed = Date.now() - start;
            const percentage = Math.max(100 - (elapsed / autoCloseTimer) * 100, 0);
            setProgress(percentage);
        }, intervalDelay);

        const timeout = setTimeout(() => {
            clearInterval(interval);
            setIsDisplayed(false);
        }, autoCloseTimer + intervalDelay);

        return () => {
            clearInterval(interval);
            clearTimeout(timeout);
        };
    }, [autoCloseTimer]);

    return (
        isDisplayed && (
            <article
                onClick={() => setIsDisplayed(false)}
                className=""
            >
                <div className={clsx(
                    "relative flex gap-2 px-4 py-2 w-fit max-w-9/10 h-fit border-2 rounded-lg hover:cursor-pointer font-bold overflow-hidden z-[9999]",
                    snackBarTypes[type].backgroundColor,
                    snackBarTypes[type].borderColor,
                    snackBarTypes[type].color,
                    className
                )}>
                    {snackBarTypes[type].icon !== null &&
                        <span className="self-center">
                            <Icon
                                name={snackBarTypes[type].icon}
                                color={snackBarTypes[type].color}
                                size={8}
                            />
                        </span>
                    }

                    <p className="self-center break-word">
                        {message}
                    </p>

                    <div className="absolute bottom-0 left-0 h-[3px] w-full overflow-hidden">
                        <div
                            className="h-full bg-black transition-all duration-100"
                            style={{ width: `${progress}%` }}
                        ></div>
                    </div>
                </div>
            </article>
        )
    )
}

export default Snackbar;