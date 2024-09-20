import React, {useEffect, useState} from 'react';
import {useDispatch, useSelector} from 'react-redux';
import {reactions} from '../util/reactions';
import {
  deletePrayerResponseReaction,
  fetchPrayerResponseReactions,
  reactToResponse
} from '../redux/prayerResponseReactionsSlice';
import Card from 'react-bootstrap/Card';
import Button from 'react-bootstrap/Button';
import {Link} from 'react-router-dom';
import {ReactionBarSelector, ReactionCounter} from '@charkour/react-reactions';
import ElapsedTime from "./ElapsedTime";

const PrayerResponseCard = ({ response }) => {
  const dispatch = useDispatch();
  const [showReactionBar, setShowReactionBar] = useState(false);
  const responseReactions = useSelector((state) => state.prayerResponseReactions.reactions[response.id]);
  const currentUser = useSelector((state) => state.user.userProfile);
  const webSocketStatus = useSelector((state) => state.webSocketState.status);
  const currentResponseReaction = currentUser && responseReactions ? responseReactions.find(x => x.user.userID === currentUser.userID) : null;
  const currentReactionEmoji = currentResponseReaction ? reactions.find(x => x.key ===
    currentResponseReaction.reactionType).node : <div>🙏</div>;
  const counterObjects = responseReactions ? responseReactions.map((reaction) => {
    const node = reactions.find(x => x.key === reaction.reactionType).node;
    const label = reactions.find(x => x.key === reaction.reactionType).label;
    const by = reaction.user.name;
    return {node, label, by};
  }) : []

  const user = response.user;
  const fullHandle = user.domain ? '@' + user.handle + '@' + user.domain : '@' + user.handle;

  const handleClickReaction = () => {
    if (currentResponseReaction) {
      dispatch(deletePrayerResponseReaction({responseId: response.id, reactionId: currentResponseReaction.id}));
    } else {
      setShowReactionBar(true);
    }
  }

  const handleChooseReaction = (key) => {
    dispatch(reactToResponse({
      form: {
        reactionType: key
      },
      responseId: response.id
    }));
    setShowReactionBar(false);
  }

  useEffect(() => {
    dispatch(fetchPrayerResponseReactions({responseId: response.id}))
  }, [dispatch, response.id]);

  useEffect(() => {
    if (webSocketStatus === 'connected') {
      dispatch({type: 'webSocket/send', payload: {type: 'subscribe', channel: `response.${response.id}.reactions`}})
    }
    return () => {
      if (webSocketStatus === 'connected') {
        dispatch({type: 'webSocket/send', payload: {type: 'unsubscribe', channel: `response.${response.id}.reactions`}})
      }
    }
  }, [dispatch, webSocketStatus, response.id]);

  return (<Card onClick={() => {
    if (showReactionBar) {
      setShowReactionBar(false);
    }
  }} style={{borderRadius: '0'}}>
    <Card.Header style={{backgroundColor: '#f8f9fa', color: '#51575d', border: 'none', borderRadius: '0'}} className='d-flex align-items-center justify-content-between'>
      <Link to={`/profiles/${fullHandle}`} className='card-link' >
        <div className='d-flex align-items-center'>
          {user.avatarURL && (
            <img src={user.avatarURL}
                 alt={`${user.name}'s avatar`}
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
      <ElapsedTime startTime={response.whenCreated} />
    </Card.Header>
    <Card.Body style={{backgroundColor: '#f8f9fa', color: '#51575d', borderRadius: '0'}}>
      <Card.Text>{response.response}</Card.Text>
      {counterObjects.length > 0 && currentUser &&
        <ReactionCounter style={{border: 'none'}} reactions={counterObjects} user={currentUser.name}
                         showOthersAlways={false}/>}
      {counterObjects.length > 0 && !currentUser && <ReactionCounter style={{border: 'none'}} reactions={counterObjects} showOthersAlways={false}/>}
    </Card.Body>
    {currentUser && <Card.Footer style={{backgroundColor: '#f8f9fa', color: '#51575d', border: "none", borderRadius: '0'}} className='d-flex justify-content-start'>
      {showReactionBar && <ReactionBarSelector style={{zIndex: 100}} reactions={reactions} onSelect={handleChooseReaction}/>}
      {!showReactionBar && <Button variant={currentResponseReaction ? 'primary' : 'light'} className='mx-2' onClick={handleClickReaction}>
        <span className='reactions'>{currentReactionEmoji}</span></Button>}
    </Card.Footer>}
  </Card>)
};

export default PrayerResponseCard;
