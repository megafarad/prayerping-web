import React from 'react';
import { Link } from 'react-router-dom';
import Container from 'react-bootstrap/Container';
import Nav from 'react-bootstrap/Nav';
import Navbar from 'react-bootstrap/Navbar';
import { useSelector } from 'react-redux';
import { useTranslation } from 'react-i18next';

const Header = () => {
  const { t } = useTranslation();
  const userProfile = useSelector((state) => state.user.userProfile);

  return (
    <Navbar expand='lg' bg='dark' data-bs-theme='dark'>
      <Container>
        <Navbar.Brand><Link to='/' className='nav-link'>PrayerPing</Link></Navbar.Brand>
        <Navbar.Toggle aria-controls='basic-navbar-nav'/>
        <Navbar.Collapse id='basic-navbar-nav' className='justify-content-end'>
          <Nav className='ml-auto'>
            { userProfile ? <Link to='/' className='nav-link'>{userProfile.fullName}</Link> : null}
            { userProfile ? <Link to='/signOut' className='nav-link'>{t('sign.out')}</Link> : null }
            { !userProfile ?
                <>
                  <Link to='/signIn' className='nav-link'>{t('sign.in')}</Link>
                  <Link to='/signUp' className='nav-link'>{t('sign.up')}</Link>
                </> : null
            }
          </Nav>
        </Navbar.Collapse>
      </Container>
    </Navbar>
  );
};

export default Header;
