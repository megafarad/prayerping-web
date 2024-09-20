import React from 'react';
import { PiHandsPrayingFill } from 'react-icons/pi';
import { MdHome } from 'react-icons/md';
import { CgProfile } from 'react-icons/cg';
import { MdLogin } from 'react-icons/md';
import { TiFolderAdd } from 'react-icons/ti';
import { MdLogout } from 'react-icons/md';
import { LiaGlobeSolid } from 'react-icons/lia';
import { Sidebar, Menu, MenuItem } from 'react-pro-sidebar';
import { NavLink } from 'react-router-dom';
import { useSelector } from 'react-redux';
import { useTranslation } from 'react-i18next';
import Footer from "./Footer";
import useIsMobileView from "../hooks/useIsMobileView";

const MainMenu = () => {
  const { t } = useTranslation();
  const userProfile = useSelector((state) => state.user.userProfile);

  const collapsed = useIsMobileView();

  return (
    <Sidebar collapsed={collapsed} rootStyles={{
      position: 'fixed',
      top: 0,
      height: '100vh',
      display: 'flex',
      flexDirection: 'column',
      justifyContent: 'space-between',
    }}>
      <div className='sidebar-header'>
        <PiHandsPrayingFill className='sidebar-icon mx-3'/>
        {!collapsed && <span className='sidebar-text'>PrayerPing</span>}
      </div>
      <Menu menuItemStyles={{
        button: {
          [`&.active`]: {
            backgroundColor: '#13395e',
            color: '#b6c8d9',
          },
        },
      }}>
        {userProfile && <MenuItem icon={<MdHome/>} component={<NavLink exact='true' to='/'/>}> {t('home')}</MenuItem>}
        <MenuItem icon={<LiaGlobeSolid/>} component={<NavLink exact='true' to='/prayers'/>}>
          {t('all.prayer.requests')}</MenuItem>
        { userProfile ? <MenuItem icon={<CgProfile/>} component={<NavLink to='/profile'/>}> Profile</MenuItem> : null}
        { userProfile ? <MenuItem icon={<MdLogout/>} component={<NavLink to={'/signOut'}/>}>{t('sign.out')}</MenuItem> :
          <>
            <MenuItem icon={<MdLogin/>} component={<NavLink to='/signIn'/>}>{t('sign.in')}</MenuItem>
            <MenuItem icon={<TiFolderAdd/>} component={<NavLink to='/signUp'/> }>{t('sign.up')}</MenuItem>
          </>}
      </Menu>
      { !collapsed && <Footer/> }
    </Sidebar>
  );
};

export default MainMenu;
