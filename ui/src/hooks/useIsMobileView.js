import {useEffect, useState} from 'react';

const useIsMobileView = () => {
  const [isMobileView, setIsMobileView] = useState(false);
  const handleResize = () => {
    setIsMobileView(window.innerWidth <= 768);
  };

  useEffect(() => {
    window.addEventListener('resize', handleResize);
    handleResize(); // Initial check
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  return isMobileView;
}

export default useIsMobileView;
