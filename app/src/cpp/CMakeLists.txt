cmake_minimum_required(VERSION 3.22.1)
project(sbx)

add_library(sbx SHARED
        sbx_bridge.cpp
        sbx_hook.cpp)

find_library(log-lib log)

target_link_libraries(sbx
        ${log-lib}
        android)

target_compile_options(sbx PRIVATE -fvisibility=hidden)
