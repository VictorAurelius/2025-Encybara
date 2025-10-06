import React, { useState } from 'react';
import Calendar from "react-calendar";
import Card from "components/card";
import "react-calendar/dist/Calendar.css";
import { MdChevronLeft, MdChevronRight } from "react-icons/md";
import "assets/css/MiniCalendar.css";

const MiniCalendar = () => {
    const [date, setDate] = useState<Date | null>(null);

    const onChange = (value: Date | Date[], event: React.MouseEvent<HTMLButtonElement, MouseEvent>) => {
        if (Array.isArray(value)) {
            setDate(value[0]);
        } else {
            setDate(value);
        }
    };

    return (
        <div>
            <Card extra="flex w-full h-full flex-col px-3 py-3">
                <Calendar
                    onChange={onChange}
                    value={date}
                    prevLabel={<MdChevronLeft className="ml-1 h-6 w-6 " />}
                    nextLabel={<MdChevronRight className="ml-1 h-6 w-6 " />}
                    view={"month"}
                />
            </Card>
        </div>
    );
};

export default MiniCalendar;
