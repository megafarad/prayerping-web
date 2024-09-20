import React from 'react';
import ButtonToolbar from 'react-bootstrap/ButtonToolbar';
import ButtonGroup from 'react-bootstrap/ButtonGroup';
import Button from 'react-bootstrap/Button';
import {Link} from 'react-router-dom';
import {FaSearch} from 'react-icons/fa';
import {useTranslation} from 'react-i18next';
import useIsMobileView from '../hooks/useIsMobileView';
import {useSelector} from 'react-redux';

const MobileButtonToolbar = () => {
  const {t} = useTranslation();
  const user = useSelector((state) => state.user.userProfile);

  const isMobileView = useIsMobileView();

  return (isMobileView && user && (
    <ButtonToolbar>
      <ButtonGroup>
        <Link to='/publish'>
          <Button className='mx-1'>
            {t('submit.request')}
          </Button>
        </Link>
      </ButtonGroup>
      <ButtonGroup>
        <Link to='/search'>
          <Button>
            <FaSearch/>
          </Button>
        </Link>
      </ButtonGroup>
    </ButtonToolbar>
  ));
};

export default MobileButtonToolbar;
