import { useState, useEffect } from 'react'

const EditFetch = url => {
    const [state, setState] = useState([null, false])

    useEffect(() => {
        setState([null, true]);

        (async () => {
            const data = await fetch(url)
                .then(res => res.json())

            setState([data.body, false])
        })()
    }, [url])

    return state
};

export default EditFetch