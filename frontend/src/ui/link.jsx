import React from 'react'
import { Link as RouterLink } from 'react-router-dom'
import clsx from 'clsx';

const Link = ({ to, children, className }) => {
  return (<>
    <RouterLink className={clsx("text-primary font-light text-lg uppercase tracking-wide hover:underline sm:text-2xl", className)} to={to}>{children}</RouterLink>
  </>)
}

export default Link