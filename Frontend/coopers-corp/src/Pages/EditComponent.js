import {useState, useEffect} from 'react';

export default function EditComponent(props) {
   const [data, setData] = useState({EMPLOYEE_ID: "", FIRST_NAME: "", LAST_NAME: "", STATUS: ""})
   //useEffect(() => {setData(props);}, [props])
   console.log(props.data);
        return (
           <div>
            hi
           </div>
        )
    }

