use std::io;
use jni::errors::{Error as JniError};

pub enum UnifiedError {
    Jni(JniError),
    Io(io::Error),
}

pub trait UnifyErrors<T> {
    fn unify_errors(self) -> Result<T, UnifiedError>;
}

impl<T> UnifyErrors<T> for Result<T, io::Error> {
    fn unify_errors(self) -> Result<T, UnifiedError> {
        match self {
            Ok(t) => Ok(t),
            Err(err) => Err(UnifiedError::Io(err))
        }
    }
}

impl<T> UnifyErrors<T> for Result<T, JniError> {
    fn unify_errors(self) -> Result<T, UnifiedError> {
        match self {
            Ok(t) => Ok(t),
            Err(jni_error) => Err(UnifiedError::Jni(jni_error))
        }
    }
}