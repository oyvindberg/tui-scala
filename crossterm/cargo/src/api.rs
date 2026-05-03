use std::convert::TryInto;
use std::io::{stdout, Write};
use std::time::Duration;

use crossterm::{cursor, event, terminal};
use jni::{
    JNIEnv,
    objects::{JClass, JObject},
    sys::{jboolean, jobject},
};

use crate::{
    jni_from_jvm,
    jni_to_jvm,
    jvm_unwrapper::JvmUnwrapper,
    unify_errors::UnifyErrors,
};

#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_terminalSize(
    env: JNIEnv,
    _class: JClass,
) -> jobject {
    let xy = terminal::size().unify_errors().and_then(|(x, y)| jni_to_jvm::xy(env, x, y).unify_errors());
    return xy.jvm_unwrap(env).into_raw();
}

#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_cursorPosition(
    env: JNIEnv,
    _class: JClass,
) -> jobject {
    let (x, y) = cursor::position().jvm_unwrap(env);
    let result = jni_to_jvm::xy(env, x, y);
    return result.jvm_unwrap(env).into_raw();
}

#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_flush(env: JNIEnv, _class: JClass) {
    std::io::stdout().flush().jvm_unwrap(env);
}

#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_disableRawMode(
    env: JNIEnv,
    _class: JClass,
) {
    terminal::disable_raw_mode().jvm_unwrap(env)
}

#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enableRawMode(env: JNIEnv, _class: JClass) {
    terminal::enable_raw_mode().jvm_unwrap(env)
}

#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_poll(
    env: JNIEnv,
    _class: JClass,
    timeout: JObject,
) -> jboolean {
    let secs_jlong = env.get_field(timeout, "secs", "J").and_then(|x| x.j()).jvm_unwrap(env);
    let nanos_jint = env.get_field(timeout, "nanos", "I").and_then(|x| x.i()).jvm_unwrap(env);
    let duration = Duration::new(
        secs_jlong.try_into().unwrap_or_default(),
        nanos_jint.try_into().unwrap_or_default(),
    );
    let res = event::poll(duration).jvm_unwrap(env);
    return res.into();
}

#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_read(
    env: JNIEnv,
    _class: JClass,
) -> jobject {
    let e = event::read().unify_errors().and_then(|e| jni_to_jvm::event(env, e).unify_errors());
    return e.jvm_unwrap(env).into_raw();
}

#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueue(
    env: JNIEnv,
    _class: JClass,
    commands_list_object: JObject,
) {
    let mut stdout1 = stdout();
    let writer = stdout1.by_ref();
    jni_from_jvm::queue_commands(writer, env, commands_list_object).jvm_unwrap(env);
}

#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_execute(
    env: JNIEnv,
    _class: JClass,
    commands_list_object: JObject,
) {
    let mut stdout1 = stdout();
    let writer = stdout1.by_ref();
    jni_from_jvm::queue_commands(writer, env, commands_list_object).jvm_unwrap(env);
    writer.flush().jvm_unwrap(env);
}
