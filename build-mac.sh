#!/bin/bash

INC=/Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/MacOSX10.8.sdk/System/Library/Frameworks/JavaVM.framework/Versions/A/Headers/

g++ "-I$INC" -c ThreadUtil.cpp
g++ -dynamiclib -o libthreadutil.jnilib ThreadUtil.o

