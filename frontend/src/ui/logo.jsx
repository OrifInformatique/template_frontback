import React from 'react'
import clsx from 'clsx'

const Logo = ({ className}) => {
  return (<>
    <img className={clsx("w-32 sm:w-64", className)} src="images/logo.svg" />
  </>)
}

export default Logo