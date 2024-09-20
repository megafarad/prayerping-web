import {addPrayer, deletePrayer, updatePrayer} from './prayerFeedSlice';
import { setStatus } from './webSocketStateSlice';
import {addPrayerReaction, removePrayerReaction} from './prayerReactionsSlice';
import {
  addPrayerResponseReducer,
  removePrayerResponseReducer,
  updatePrayerResponseReducer
} from './prayerResponseSlice';
import {addPrayerResponseReaction, removePrayerResponseReaction} from "./prayerResponseReactionsSlice";

export const webSocketMiddleware = (socket) => (store) => (next) => (action) => {
  const { type } = action;

  switch (type) {
    case 'webSocket/connect':
      store.dispatch(setStatus('connecting'));
      socket.connect('ws://localhost:9000/ws');
      socket.on('open', () => {
        store.dispatch(setStatus('connected'));
        console.log('WebSocket Connected');
      });
      socket.on('message', (message) => {
        const messageData = JSON.parse(message.data);
        if (messageData && messageData.type) {
          switch (messageData.type) {
            case 'newPrayerRequest':
              store.dispatch(addPrayer(messageData.request));
              break;
            case 'updatePrayerRequest':
              store.dispatch(updatePrayer(messageData.request));
              break;
            case 'deletePrayerRequest':
              store.dispatch(deletePrayer(messageData.requestId));
              break;
            case 'newPrayerRequestReaction':
              store.dispatch(addPrayerReaction(messageData.reaction));
              break;
            case 'deletePrayerRequestReaction':
              store.dispatch(removePrayerReaction(messageData.reaction));
              break;
            case 'newPrayerResponse':
              store.dispatch(addPrayerResponseReducer(messageData.response));
              break;
            case 'updatePrayerResponse':
              store.dispatch(updatePrayerResponseReducer(messageData.response));
              break;
            case 'deletePrayerResponse':
              store.dispatch(removePrayerResponseReducer(messageData.response));
              break;
            case 'newPrayerResponseReaction':
              store.dispatch(addPrayerResponseReaction(messageData.reaction));
              break;
            case 'deletePrayerResponseReaction':
              store.dispatch(removePrayerResponseReaction(messageData.reaction));
              break;
            default:
              console.log('Unhandled WebSocketMessage: ', messageData)
          }
        }
      });
      socket.on('close', () => {
        store.dispatch(setStatus('disconnected'));
        console.log('WebSocket Closed');
      });
      socket.on('error', (error) => {
        store.dispatch(setStatus('disconnected'));
        console.error('WebSocket Error', error);
      })
      break;
    case 'webSocket/disconnect':
      socket.disconnect();
      break;
    case 'webSocket/send':
      socket.send(action.payload);
      break;
    default:
      break;
  }
  return next(action);
}
