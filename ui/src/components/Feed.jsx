import React, { useEffect, useRef, useCallback } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { fetchPrayerFeed, resetPrayerFeed } from '../redux/prayerFeedSlice';
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import Spinner from 'react-bootstrap/Spinner';
import Alert from 'react-bootstrap/Alert';
import Layout from './Layout';
import PrayerRequestCard from './PrayerRequestCard';
import { useTranslation } from 'react-i18next';

const Feed = ({ allPrayers }) => {
  const dispatch = useDispatch();
  const prayers = useSelector((state) => state.prayerFeed.prayers);
  const userProfile = useSelector((state) => state.user.userProfile);
  const webSocketStatus = useSelector((state) => state.webSocketState.status);
  const page = useSelector((state) => state.prayerFeed.page);
  const status = useSelector((state) => state.prayerFeed.status);
  const hasMore = useSelector((state) => state.prayerFeed.hasMore);

  const { t } = useTranslation();

  // Fetch initial data
  useEffect(() => {
    dispatch(fetchPrayerFeed({ page: 0, allPrayers }));
    return () => {
      dispatch(resetPrayerFeed());
    };
  }, [dispatch, allPrayers]);

  useEffect(() => {
    if (userProfile && webSocketStatus === 'disconnected') {
      dispatch({type: 'webSocket/connect'});
    }

    if (userProfile && webSocketStatus === 'connected') {
      //Subscribe to correct channel
      if (allPrayers) {
        dispatch({type: 'webSocket/send', payload: {type: 'subscribe', channel: 'public.local'}});
      } else {
        dispatch({type: 'webSocket/send', payload: {type: 'subscribe', channel: `user.${userProfile.userID}`}});
      }
    }

    return () => {
      if (userProfile && webSocketStatus === 'connected') {
        if (allPrayers) {
          dispatch({type: 'webSocket/send', payload: {type: 'unsubscribe', channel: 'public.local'}});
        } else {
          dispatch({type: 'webSocket/send', payload: {type: 'unsubscribe', channel: `user.${userProfile.userID}`}});
        }
      }
    }
  }, [dispatch, userProfile, webSocketStatus, allPrayers]);

  // IntersectionObserver callback
  const observer = useRef();
  const lastPrayerElementRef = useCallback(
    (node) => {
      if (status === 'loading') return;
      if (observer.current) observer.current.disconnect();
      observer.current = new IntersectionObserver((entries) => {
        if (entries[0].isIntersecting && hasMore) {
          dispatch(fetchPrayerFeed({ page: page, allPrayers }));
        }
      });
      if (node) observer.current.observe(node);
    },
    [status, hasMore, dispatch, page, allPrayers]
  );

  return (
    <Layout headerTitle={t('prayer.requests')}>
      {prayers.map((prayer, index) => {
        if (index === prayers.length - 1) {
          return (
            <Row key={index} ref={lastPrayerElementRef}>
              <Col xs={12} sm={12} md={12} lg={12}>
                <PrayerRequestCard prayer={prayer} />
              </Col>
            </Row>
          );
        } else {
          return (
            <Row key={index}>
              <Col xs={12} sm={12} md={12} lg={12}>
                <PrayerRequestCard prayer={prayer} />
              </Col>
            </Row>
          );
        }
      })}

      {status === 'loading' && (
        <div className='text-center my-4'>
          <Spinner animation='border' />
        </div>
      )}
      {status === 'failed' && (
        <Alert variant='danger' className='text-center my-4'>
          {t('prayer.requests.error')}
        </Alert>
      )}
      {!hasMore && (
        <div className='text-center my-4'>
          <Alert variant='info'>No more prayers to load.</Alert>
        </div>
      )}
    </Layout>
  );
};

export default Feed;
