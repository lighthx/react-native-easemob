import {NativeModules} from 'react-native';

const {RNReactNativeEasemob} = NativeModules;
export default RNReactNativeEasemob;
const
	{
		_login ,
		_create,
		_logout,
		_sendText,
		_sendVoice,
		_sendVideo,
		_sendImage,
		_sendLocation,
		_addMessageListener,
		_getAllContacts,
		_getAllConversations,
		_getConversation,
		_download,
		_loadPics,
		_searchText,
		_deleteMsg,
		_forwarding
	} = RNReactNativeEasemob
export async function login(user,pwd){
	try{
		const result = await _login(user,pwd)
		_addMessageListener()
		return result
	}catch (e){
		console.log(e)
	}

}

export async function create(user,pwd){
	try {
		const result = await _create(user,pwd)
		return result
	}
	catch (e){
		console.log(e)
	}
}
export async function logout(){
	try{
		const result = await _logout()
		return result
	}catch (e){
		console.log(e)
	}

}

export async function sendText(content,user){

	try{
		const result = await _sendText(content,user)
		console.log(result)
		return result
	}catch (e){
		console.log(e)
	}

}
export async function sendVoice(path,length,user){
 try{
	 const result = await _sendVoice(path,length,user,)
	 return result
 }catch (e){
	console.log(e)
 }

}
export async function sendVideo(path,thumbPath,length,user){
try{
	const result = await _sendVideo(path,thumbPath,length,user)
	return result
}catch (e){
	console.log(e)
}

}
export async function sendImage(path,user){

	try{
		const result = await _sendImage(path,user)
		console.log(result)
		return result
	}catch (e){
			console.log(e)
	}

}
export async function sendLocation(latitude,longitude,address,user){
	console.log(latitude,address)
	try{
		const result = await _sendLocation(latitude-0,longitude-0,address,user)
		return result
	}catch (e){
		console.log(e)
	}

}
export async function getAllContacts(){
	try{
		const result = await _getAllContacts()
		return result
	}
	catch (e){
		console.log(e)
	}

}
export async function getAllConversations(){
	try{
		const result = await _getAllConversations()
		return result
	}catch (e){
		console.log(e)
	}

}
export async function getConversation(user,msgId){
	try{
		const result = await _getConversation(user,msgId)
		return result
	}catch (e){
		console.log(e)
	}

}
export async function download(user,msgId){
	try{
		const result=await _download(user,msgId);
		return result
	}catch (e){
		console.log(e)
	}

}
export async function loadPics(user){
	try{
		const result=await _loadPics(user)
		return result
	}catch (e){
		console.log(e)
	}
}
export async function searchText(user ,keyword){
	try{
		const result=await _searchText(user,keyword)
		return result
	}catch(e){
		console.log(e)
	}
}
export async function deleteMsg(user,msgId){
	console.log('params')
	console.log(user,msgId)
	try{
		const result=await _deleteMsg(user,msgId)
		console.log(result)
		return result
	}catch (e){
		console.log("失败")
	}
}
export async function forwarding(user,msgId,toUser){
	console.log('params')
	console.log(user,msgId,toUser)
	try{
		const result=await _forwarding(user,msgId,toUser)
		console.log(result)
		return result
	}catch (e){
		console.log("失败")
	}
}