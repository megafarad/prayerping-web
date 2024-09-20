import React from 'react';
import Container from 'react-bootstrap/Container';
import MainMenu from './MainMenu';
import { useSelector } from 'react-redux';
import ApiResponse from './ApiResponse';
import PrayerRequestForm from './PrayerRequestForm';
import SearchInputForm from './SearchInputForm';
import useIsMobileView from '../hooks/useIsMobileView';
import MobileButtonToolbar from './MobileButtonToolbar';

const Layout = ({ headerTitle, children }) => {
  const user = useSelector((state) => state.user.userProfile);

  const isMobileView = useIsMobileView();

  return (
    <div className='app-container'>
      <div className='content-container'>
        <MainMenu />
        <div className='main-content'>
          <div className='header'>
            <h1>{headerTitle}</h1>
            <MobileButtonToolbar/>
          </div>
          <div className='content'>
            <Container fluid>
              <ApiResponse/>
              {children}
            </Container>
          </div>
        </div>
        {!isMobileView && user && <div className='main-forms'>
          <div className='header search-header'>
            <SearchInputForm/>
          </div>
          <PrayerRequestForm action='/publishSidebar'/>
        </div>}
      </div>
    </div>
  );
};

export default Layout;
