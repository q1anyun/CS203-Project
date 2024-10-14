@echo off
setlocal

rem
:wait_for_service
set "port=%1"
set "retries=15"
set "wait=5"
set /a "count=0"

echo Waiting for service on port %port% to be ready...

:retry_loop
rem
powershell -Command "try { (New-Object Net.Sockets.TcpClient('localhost', %port%)).Close(); exit 0 } catch { exit 1 }"
if %errorlevel%==0 (
    echo Service on port %port% is up and running!
    goto :eof
) else (
    set /a "count+=1"
    if %count% geq %retries% (
        echo Service on port %port% failed to start after %retries% retries.
        exit /b 1
    )
    echo Service not yet available. Retrying in %wait% seconds...
    timeout /t %wait% >nul
    goto retry_loop
)

:eof
exit /b 0


rem Function to run a service
:run_service
set "service_name=%1"
echo Starting %service_name%...
start cmd /c "cd /d %service_name% && gradlew bootRun"
exit /b 0


rem Start Spring Cloud Config server using Gradle
echo Starting Spring Cloud Config server...
start cmd /c "cd /d config-server && gradlew bootRun"

rem Wait for Config server to be ready (assuming it runs on port 8888)
call :wait_for_service 8888

rem Start the other microservices once Config server is ready
echo Starting other microservices...

call :run_service gateway
call :run_service auth-service
call :run_service player-service
call :run_service user-service
call :run_service tournament-service
call :run_service match-service
call :run_service elo-service

echo All services started successfully!

rem
pause