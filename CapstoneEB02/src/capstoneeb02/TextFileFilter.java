/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package capstoneeb02;

import java.io.File;
import java.io.FileFilter;

/**
 *
 * @author kingtahir
 */
public class TextFileFilter implements FileFilter{

@Override
   public boolean accept(File pathname) {
      return pathname.getName().toLowerCase().endsWith(".html") || pathname.getName().toLowerCase().endsWith(".htm");
   }
    
}