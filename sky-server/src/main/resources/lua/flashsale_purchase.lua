local stockKey = KEYS[1]
local userCountKey = KEYS[2]
local activityInfoKey = KEYS[3]

local userId = ARGV[1]
local nowTime = tonumber(ARGV[2])

if redis.call('exists', activityInfoKey) == 0 then
    return -1
end

local status = tonumber(redis.call('hget', activityInfoKey, 'status'))
local startTime = tonumber(redis.call('hget', activityInfoKey, 'startTime'))
local endTime = tonumber(redis.call('hget', activityInfoKey, 'endTime'))
local limitPerUser = tonumber(redis.call('hget', activityInfoKey, 'limitPerUser'))

if status == nil or status ~= 1 then
    return -1
end

if startTime ~= nil and nowTime < startTime then
    return -4
end

if endTime ~= nil and nowTime > endTime then
    return -5
end

local stock = tonumber(redis.call('get', stockKey))
if stock == nil then
    return -1
end

if stock <= 0 then
    return -2
end

local purchasedCount = tonumber(redis.call('hget', userCountKey, userId))
if purchasedCount ~= nil and purchasedCount >= limitPerUser then
    return -3
end

redis.call('decrby', stockKey, 1)
redis.call('hincrby', userCountKey, userId, 1)
return 1
