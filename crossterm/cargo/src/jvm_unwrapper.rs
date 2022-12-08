use std::io;

use jni::{
    errors::{Error as JniError, Result as JniResult},
    JNIEnv,
};

use crate::unify_errors::UnifiedError;

// ensure that we always throw a JVM exception instead of `panic`ing
pub trait JvmUnwrapper<T> {
    fn jvm_unwrap(self, env: JNIEnv) -> T;
}

impl<T> JvmUnwrapper<T> for Result<T, io::Error> where T: Default {
    fn jvm_unwrap(self, env: JNIEnv) -> T {
        match self {
            Ok(t) => t,
            Err(err) => handle_error(env, err)
        }
    }
}

impl<T> JvmUnwrapper<T> for JniResult<T> where T: Default {
    fn jvm_unwrap(self, env: JNIEnv) -> T {
        match self {
            Ok(t) => t,
            Err(err) => handle_jni_error(env, err)
        }
    }
}

impl<T> JvmUnwrapper<T> for Result<T, UnifiedError> where T: Default {
    fn jvm_unwrap(self, env: JNIEnv) -> T {
        match self {
            Ok(t) => t,
            Err(UnifiedError::Jni(jni_error)) => handle_jni_error(env, jni_error),
            Err(UnifiedError::Io(err)) => handle_error(env, err),
        }
    }
}

fn handle_jni_error<T>(env: JNIEnv, jni_error: JniError) -> T where T: Default {
    match jni_error {
        JniError::JavaException => {
            env.throw(env.exception_occurred().unwrap()).unwrap();
            T::default()
        }
        err => {
            let runtime_exception = env.find_class("java/lang/RuntimeException").unwrap();
            env.throw_new(runtime_exception, format!("Error from JNI: {err:?}")).unwrap();
            T::default()
        }
    }
}

fn handle_error<T>(env: JNIEnv, err: io::Error) -> T where T: Default {
    let runtime_exception = env.find_class("java/lang/RuntimeException").unwrap();
    env.throw_new(runtime_exception, format!("IO error: {err:?}")).unwrap();
    T::default()
}
