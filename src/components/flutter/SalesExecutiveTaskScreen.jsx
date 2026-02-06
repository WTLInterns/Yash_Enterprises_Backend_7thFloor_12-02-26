import React, { useState, useEffect } from 'react';
import FlutterLocationService from '../../services/flutter/LocationService';
import { 
    MapPin, 
    Clock, 
    Camera, 
    CheckCircle, 
    AlertCircle, 
    Navigation,
    User,
    Phone
} from 'lucide-react';

/**
 * 📱 Flutter-Style Sales Executive Task Screen
 * Implements the exact flow specified:
 * 1. Open Task → GPS starts
 * 2. Reach Customer (≤200m) → Auto punch-in
 * 3. Task Working → Buttons enabled only when ≤200m
 * 4. 15-min idle popup
 * 5. Complete Task → Camera → Submit
 */
const SalesExecutiveTaskScreen = ({ task, employeeId, onTaskComplete }) => {
    const [location, setLocation] = useState(null);
    const [distance, setDistance] = useState(null);
    const [punchInStatus, setPunchInStatus] = useState('NOT_STARTED');
    const [isTracking, setIsTracking] = useState(false);
    const [isLoading, setIsLoading] = useState(false);
    const [taskStatus, setTaskStatus] = useState(task.status);

    useEffect(() => {
        initializeTask();
        return () => {
            FlutterLocationService.stopTracking();
        };
    }, [task]);

    const initializeTask = async () => {
        try {
            setIsLoading(true);
            
            // 🔹 Task opens - GPS starts tracking
            console.log('🔹 Opening task:', task.id);
            
            // Set customer location if available
            if (task.customerAddress) {
                FlutterLocationService.setCustomerLocation(
                    task.customerAddress.latitude,
                    task.customerAddress.longitude,
                    task.customerAddress.address
                );
            }

            // Set up callbacks
            FlutterLocationService.onLocationUpdate(handleLocationUpdate);
            FlutterLocationService.onPunchIn(handleAutoPunchIn);

            // Start GPS tracking
            const currentLocation = await FlutterLocationService.startTracking();
            setLocation(currentLocation);
            setIsTracking(true);

            // Calculate initial distance
            updateDistance(currentLocation);

        } catch (error) {
            console.error('Failed to initialize task:', error);
            FlutterLocationService.showToast('Failed to start location tracking', 'error');
        } finally {
            setIsLoading(false);
        }
    };

    const handleLocationUpdate = (newLocation) => {
        setLocation(newLocation);
        updateDistance(newLocation);
    };

    const updateDistance = (currentLocation) => {
        if (task.customerAddress) {
            const dist = FlutterLocationService.calculateDistance(
                currentLocation.latitude, currentLocation.longitude,
                task.customerAddress.latitude, task.customerAddress.longitude
            );
            setDistance(dist);
        }
    };

    const handleAutoPunchIn = (punchData) => {
        console.log('🔹 Auto punch-in detected:', punchData);
        setPunchInStatus('PUNCHED_IN');
        
        // Update task status to IN_PROGRESS
        setTaskStatus('IN_PROGRESS');
        
        // Send punch-in data to backend
        sendPunchInToBackend(punchData);
    };

    const sendPunchInToBackend = async (punchData) => {
        try {
            // API call to backend to record punch-in
            const response = await fetch(`/api/employee-locations/${employeeId}/location`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${localStorage.getItem('auth_token')}`
                },
                body: JSON.stringify({
                    latitude: punchData.location.latitude,
                    longitude: punchData.location.longitude,
                    accuracy: punchData.location.accuracy,
                    timestamp: punchData.timestamp,
                    trackingType: 'AUTO_PUNCH_IN'
                })
            });
            
            if (!response.ok) {
                throw new Error('Failed to record punch-in');
            }
            
        } catch (error) {
            console.error('Failed to send punch-in to backend:', error);
        }
    };

    const handleStartTask = async () => {
        if (distance > 200) {
            FlutterLocationService.showToast('Move within 200m of customer location', 'warning');
            return;
        }

        try {
            setIsLoading(true);
            
            const response = await fetch(`/api/tasks/${task.id}/status`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${localStorage.getItem('auth_token')}`
                },
                body: JSON.stringify({
                    status: 'IN_PROGRESS'
                })
            });

            if (response.ok) {
                setTaskStatus('IN_PROGRESS');
                FlutterLocationService.showToast('Task started', 'success');
            } else {
                throw new Error('Failed to start task');
            }

        } catch (error) {
            console.error('Failed to start task:', error);
            FlutterLocationService.showToast('Failed to start task', 'error');
        } finally {
            setIsLoading(false);
        }
    };

    const handleCompleteTask = async () => {
        if (distance > 200) {
            FlutterLocationService.showToast('Move within 200m of customer location', 'warning');
            return;
        }

        try {
            setIsLoading(true);

            // 🔹 Complete Task - Camera opens (live only)
            const completionData = await FlutterLocationService.completeTask(task.id);

            // Send completion data to backend
            const response = await fetch(`/api/tasks/${task.id}/complete`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${localStorage.getItem('auth_token')}`
                },
                body: JSON.stringify({
                    photoUrl: completionData.photoUrl,
                    workLat: completionData.workLat,
                    workLng: completionData.workLng,
                    completionNotes: 'Task completed via mobile app'
                })
            });

            if (response.ok) {
                setTaskStatus('COMPLETED');
                FlutterLocationService.showToast('Task completed successfully', 'success');
                
                // 🔹 Task closes, Punch-out happens
                FlutterLocationService.stopTracking();
                
                if (onTaskComplete) {
                    onTaskComplete(completionData);
                }
            } else {
                throw new Error('Failed to complete task');
            }

        } catch (error) {
            console.error('Failed to complete task:', error);
            FlutterLocationService.showToast(error.message, 'error');
        } finally {
            setIsLoading(false);
        }
    };

    const canPerformTaskActions = () => {
        return distance !== null && distance <= 200 && punchInStatus === 'PUNCHED_IN';
    };

    const getStatusColor = () => {
        switch (punchInStatus) {
            case 'PUNCHED_IN': return 'text-green-600';
            case 'NOT_PUNCHED_IN': return 'text-red-600';
            default: return 'text-gray-600';
        }
    };

    const getDistanceColor = () => {
        if (distance === null) return 'text-gray-600';
        return distance <= 200 ? 'text-green-600' : 'text-red-600';
    };

    if (isLoading) {
        return (
            <div className="min-h-screen bg-gray-50 flex items-center justify-center">
                <div className="text-center">
                    <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
                    <p className="text-gray-600">Initializing task...</p>
                </div>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-gray-50">
            {/* Header */}
            <div className="bg-blue-600 text-white p-4">
                <div className="flex items-center justify-between">
                    <div>
                        <h1 className="text-xl font-bold">{task.task_name}</h1>
                        <p className="text-blue-100 text-sm">{task.task_description}</p>
                    </div>
                    <div className="text-right">
                        <div className={`text-sm font-medium ${getStatusColor()}`}>
                            {punchInStatus.replace('_', ' ')}
                        </div>
                        <div className="text-xs text-blue-100">
                            {isTracking ? '📍 Tracking' : '📍 Not Tracking'}
                        </div>
                    </div>
                </div>
            </div>

            {/* Customer Info */}
            <div className="bg-white m-4 rounded-lg shadow-sm p-4">
                <h3 className="font-semibold text-gray-800 mb-3 flex items-center gap-2">
                    <User size={18} />
                    Customer Information
                </h3>
                <div className="space-y-2">
                    <div>
                        <span className="text-sm text-gray-600">Name:</span>
                        <p className="font-medium">{task.client?.name}</p>
                    </div>
                    <div>
                        <span className="text-sm text-gray-600">Address:</span>
                        <p className="font-medium">{task.customerAddress?.address}</p>
                    </div>
                    <div className="flex items-center gap-2">
                        <Phone size={14} className="text-gray-400" />
                        <span className="text-sm">{task.client?.contact_phone}</span>
                    </div>
                </div>
            </div>

            {/* Location Status */}
            <div className="bg-white mx-4 mb-4 rounded-lg shadow-sm p-4">
                <h3 className="font-semibold text-gray-800 mb-3 flex items-center gap-2">
                    <MapPin size={18} />
                    Location Status
                </h3>
                
                {location && (
                    <div className="space-y-2">
                        <div className="flex justify-between items-center">
                            <span className="text-sm text-gray-600">Distance to Customer:</span>
                            <span className={`font-medium ${getDistanceColor()}`}>
                                {FlutterLocationService.formatDistance(distance)}
                            </span>
                        </div>
                        
                        {distance > 200 && (
                            <div className="text-sm text-orange-600 bg-orange-50 p-2 rounded">
                                📍 Move within 200m of customer location
                            </div>
                        )}
                        
                        {distance <= 200 && punchInStatus === 'NOT_PUNCHED_IN' && (
                            <div className="text-sm text-blue-600 bg-blue-50 p-2 rounded">
                                🎯 Within range - Auto punch-in will trigger
                            </div>
                        )}
                        
                        {punchInStatus === 'PUNCHED_IN' && distance <= 200 && (
                            <div className="text-sm text-green-600 bg-green-50 p-2 rounded">
                                ✅ Ready to work
                            </div>
                        )}
                    </div>
                )}
            </div>

            {/* Task Actions */}
            <div className="bg-white mx-4 mb-4 rounded-lg shadow-sm p-4">
                <h3 className="font-semibold text-gray-800 mb-3">Task Actions</h3>
                
                <div className="space-y-3">
                    {taskStatus === 'INQUIRY' && (
                        <button
                            onClick={handleStartTask}
                            disabled={!canPerformTaskActions()}
                            className={`w-full py-3 px-4 rounded-lg font-medium transition-colors flex items-center justify-center gap-2 ${
                                canPerformTaskActions()
                                    ? 'bg-blue-600 text-white hover:bg-blue-700'
                                    : 'bg-gray-300 text-gray-500 cursor-not-allowed'
                            }`}
                        >
                            <Navigation size={18} />
                            Start Task
                        </button>
                    )}

                    {taskStatus === 'IN_PROGRESS' && (
                        <button
                            onClick={handleCompleteTask}
                            disabled={!canPerformTaskActions()}
                            className={`w-full py-3 px-4 rounded-lg font-medium transition-colors flex items-center justify-center gap-2 ${
                                canPerformTaskActions()
                                    ? 'bg-green-600 text-white hover:bg-green-700'
                                    : 'bg-gray-300 text-gray-500 cursor-not-allowed'
                            }`}
                        >
                            <Camera size={18} />
                            Complete Task
                        </button>
                    )}

                    {!canPerformTaskActions() && (
                        <div className="text-sm text-center text-orange-600">
                            {distance > 200 ? '📍 Move within 200m of customer location' : '⏰ Waiting for auto punch-in'}
                        </div>
                    )}
                </div>
            </div>

            {/* Instructions */}
            <div className="bg-blue-50 mx-4 mb-4 rounded-lg p-4">
                <h4 className="font-medium text-blue-800 mb-2">📋 Instructions:</h4>
                <ol className="text-sm text-blue-700 space-y-1">
                    <li>1. GPS tracking is active</li>
                    <li>2. Move within 200m of customer location</li>
                    <li>3. Auto punch-in will trigger</li>
                    <li>4. Complete task with live photo</li>
                </ol>
            </div>
        </div>
    );
};

export default SalesExecutiveTaskScreen;
