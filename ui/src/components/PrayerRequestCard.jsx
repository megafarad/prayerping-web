import React, { useState, useEffect } from 'react';
import PropTypes from 'prop-types';
import { useDispatch, useSelector } from 'react-redux';
import Card from 'react-bootstrap/Card';
import Button from 'react-bootstrap/Button';
import Dropdown from 'react-bootstrap/Dropdown';
import { ReactionBarSelector, ReactionCounter } from '@charkour/react-reactions';
import {deletePrayerReaction, fetchPrayerReactions, reactToPrayer} from '../redux/prayerReactionsSlice';
import { FaReply, FaEllipsisV } from 'react-icons/fa';
import {reactions} from '../util/reactions';
import {Link, useLocation} from 'react-router-dom';
import {useQueryParams} from '../hooks/useQueryParams';
import ElapsedTime from './ElapsedTime';
import {deletePrayerRequest} from '../redux/prayerRequestSlice';
import DeleteModal from './DeleteModal';
import {useTranslation} from 'react-i18next';

const PrayerRequestCard = ({ prayer }) => {
  const {t} = useTranslation();
  const location = useLocation();
  const dispatch = useDispatch();
  const [showReactionBar, setShowReactionBar] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const prayerReactions = useSelector((state) => state.prayerReactions.reactions[prayer.id]);
  const currentUser = useSelector((state) => state.user.userProfile);
  const webSocketStatus = useSelector((state) => state.webSocketState.status);
  const currentPrayerReaction = currentUser && prayerReactions ? prayerReactions.find(x => x.user.userID === currentUser.userID) : null;
  const currentReactionEmoji = currentPrayerReaction ? reactions.find(x => x.key ===
    currentPrayerReaction.reactionType).node : <div>🙏</div>;
  const counterObjects = prayerReactions ? prayerReactions.map((reaction) => {
    const node = reactions.find(x => x.key === reaction.reactionType).node;
    const label = reactions.find(x => x.key === reaction.reactionType).label;
    const by = reaction.user.name;
    return {node, label, by};
  }) : [];
  const user = prayer.user;
  const fullHandle = user.domain ? '@' + user.handle + '@' + user.domain : '@' + user.handle;

  const queryParams = useQueryParams();
  const showResponseForm = queryParams.get('showResponseForm') === 'true';

  const handleClickReaction = () => {
    if (currentPrayerReaction) {
      dispatch(deletePrayerReaction({prayerId: prayer.id, reactionId: currentPrayerReaction.id}));
    } else {
      setShowReactionBar(true);
    }

  };

  const handleChooseReaction = (key) => {
    dispatch(reactToPrayer({
      form: {
        reactionType: key
      },
      prayerId: prayer.id
    }));
    setShowReactionBar(false);
  };

  const handleShowDeleteModal = () => setShowDeleteModal(true);
  const handleCloseDeleteModal = () => setShowDeleteModal(false);

  const handleDelete = () => {
    dispatch(deletePrayerRequest(prayer.id));
    setShowDeleteModal(false);
  }

  useEffect(() => {
    dispatch(fetchPrayerReactions({prayerId: prayer.id}));
  }, [dispatch, prayer.id]);

  useEffect(() => {
    if (webSocketStatus === 'connected') {
      dispatch({type: 'webSocket/send', payload: {type: 'subscribe', channel: `prayer.${prayer.id}.reactions`}});
    }
    return () => {
      if (webSocketStatus === 'connected') {
        dispatch({type: 'webSocket/send', payload: {type: 'unsubscribe', channel: `prayer.${prayer.id}.reactions`}});
      }
    }
  }, [dispatch, webSocketStatus, prayer.id]);

  return (
    <Card onClick={() => {
      if (showReactionBar) {
        setShowReactionBar(false);
      }
    }} style={{borderRadius: '0'}}>
      <Card.Header style={{backgroundColor: '#f8f9fa', color: '#51575d', border: 'none', borderRadius: '0'}} className='d-flex align-items-center justify-content-between'>
        <Link to={`/profiles/${fullHandle}`} className='card-link' >
          <div className='d-flex align-items-center'>
            {user.avatarURL && (
              <img src={user.avatarURL}
                   alt={`${prayer.user.name}'s avatar`}
                   className='rounded-circle mx-3'
                   style={{width: '40px', height: '40px', objectFit: 'cover'}}
              />
            )}
            <div>
              <Card.Title className='mb-0'>{user.name}</Card.Title>
              <small className='text-muted'>{fullHandle}</small>
            </div>
          </div>
        </Link>
        {currentUser &&
        <Dropdown>
          <Dropdown.Toggle variant="link" id="dropdown-basic">
            <FaEllipsisV />
          </Dropdown.Toggle>
          <Dropdown.Menu>
            {prayer.canEdit && <Dropdown.Item as={Link} to={`/prayers/${prayer.id}/edit?redirectTo=${location.pathname}`}>{t('edit')}</Dropdown.Item>}
            {prayer.canEdit && <Dropdown.Item onClick={handleShowDeleteModal}>{t('delete')}</Dropdown.Item>}
          </Dropdown.Menu>
        </Dropdown>}
        <DeleteModal handleDelete={handleDelete} handleCloseModal={handleCloseDeleteModal} showModal={showDeleteModal}
                     bodyMessage={t('confirm.delete.prayer.request')}/>
        <Link to={`/prayers/${prayer.id}`} className='card-link elapsed-time'>
          <ElapsedTime startTime={prayer.whenCreated}/>
        </Link>
      </Card.Header>
      <Link to={`/prayers/${prayer.id}`} className='card-link'>
        <Card.Body style={{backgroundColor: '#f8f9fa', color: '#51575d', borderRadius: '0'}}>
          <Card.Text>{prayer.request}</Card.Text>
          {counterObjects.length > 0 && currentUser &&
            <ReactionCounter style={{border: 'none'}} reactions={counterObjects} user={currentUser.name}
                             showOthersAlways={false}/>}
          {counterObjects.length > 0 && !currentUser && <ReactionCounter style={{border: 'none'}} reactions={counterObjects} showOthersAlways={false}/>}
        </Card.Body>
      </Link>
      {currentUser && <Card.Footer style={{backgroundColor: '#f8f9fa', color: '#51575d', border: "none", borderRadius: '0'}} className='d-flex justify-content-start'>
        {showReactionBar && <ReactionBarSelector style={{zIndex: 100}} reactions={reactions} onSelect={handleChooseReaction}/>}
        {!showReactionBar && <Button variant={currentPrayerReaction ? 'primary' : 'light'} className='mx-2' onClick={handleClickReaction}>
          <span className='reactions'>{currentReactionEmoji}</span></Button>}
        <Link to={showResponseForm ? `/prayers/${prayer.id}` : `/prayers/${prayer.id}?showResponseForm=true`}>
          <Button size='lg' variant={showResponseForm ? 'primary' : 'light'} className='mx-2'>
            <FaReply size='24px'/>
          </Button>
        </Link>
      </Card.Footer>}
    </Card>
  );
};

PrayerRequestCard.propTypes = {
  prayer: PropTypes.shape({
    id: PropTypes.string.isRequired,
    user: PropTypes.shape({
      userID: PropTypes.string.isRequired,
      avatarURL: PropTypes.string,
      name: PropTypes.string.isRequired,
      handle: PropTypes.string.isRequired,
      domain: PropTypes.string,
    }).isRequired,
    whenCreated: PropTypes.string.isRequired,
    request: PropTypes.string.isRequired,
    canEdit: PropTypes.bool.isRequired,
  }).isRequired,
};

export default PrayerRequestCard;
