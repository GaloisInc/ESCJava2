package com.sun.jmx.mbeanserver;

import javax.management.*;

final class MetaDataImpl$PrivateStandardMeta extends StandardMetaDataImpl {
    /*synthetic*/ final MetaDataImpl this$0;
    
    MetaDataImpl$PrivateStandardMeta(/*synthetic*/ final MetaDataImpl this$0) {
        this.this$0 = this$0;
        
    }
    
    protected Class findClass(String className, ClassLoader loader) throws ReflectionException {
        return this$0.findClass(className, loader);
    }
    
    protected Class[] findSignatureClasses(String[] signature, ClassLoader loader) throws ReflectionException {
        return this$0.findSignatureClasses(signature, loader);
    }
}
