import React from 'react'
import clsx from 'clsx'

const Logo = ({ className}) => {
  return (<>
    <img className={clsx("w-48 sm:w-96", className)} src="images/logo.svg" />
  </>)
}

export default Logo