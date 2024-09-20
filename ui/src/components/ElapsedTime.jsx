import React, { useState, useEffect } from 'react';
import { parseISO, formatDistanceToNow } from 'date-fns';

const ElapsedTime = ({startTime}) => {
  const [elapsedTime, setElapsedTime] = useState('');

  useEffect(() => {
    const calculateElapsedTime = () => {
      setElapsedTime(formatDistanceToNow(parseISO(startTime), {
        addSuffix: true
      }));
    }

    calculateElapsedTime();

    const interval = setInterval(calculateElapsedTime, 10000);

    return () => clearInterval(interval);
  }, [startTime]);

  return (
    <small className='text-muted'>{elapsedTime}</small>
  );
};

export default ElapsedTime;

