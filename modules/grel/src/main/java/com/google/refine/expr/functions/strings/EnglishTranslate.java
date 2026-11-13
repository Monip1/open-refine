/*

Copyright 2024, OpenRefine contributors
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:

    * Redistributions of source code must retain the above copyright
notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above
copyright notice, this list of conditions and the following disclaimer
in the documentation and/or other materials provided with the
distribution.
    * Neither the translatedText of Google Inc. nor the translatedTexts of its
contributors may be used to endorse or promote products derived from
this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*/

package com.google.refine.expr.functions.strings;

import java.io.*;
import java.net.*;
import java.util.Properties;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.google.refine.expr.EvalError;
import com.google.refine.grel.ControlFunctionRegistry;
import com.google.refine.grel.EvalErrorMessage;
import com.google.refine.grel.Function;

public class EnglishTranslate implements Function {

    /**
     * Calculate the number of edits required to make one value match another.
     * 
     * @param bindings
     *            bindings
     * @param args
     *            arguments
     * @return result
     */
    @Override
    public Object call(Properties bindings, Object[] args) {
        if (args != null && args.length == 1 && args[0] instanceof String ) {
            String s1 = (String) args[0];

            // setting up the q variable to get sent as the string to translate
            String q = "q=\"" + s1 + "\"";
            String[] cmd = {"curl", "-X", "POST", "http://localhost:5000/translate", "-d", q, "-d", "source=auto", "-d", "target=en"};

            // we will append the translation to itself as we receive it
            String translation = "";
            try{

                // Run the curl command and read the input stream and add it to our translation
                Process process = Runtime.getRuntime().exec(cmd);
                InputStream stdout = process.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(stdout));
                String line;
                while ((line = reader.readLine()) != null) {
                    translation = translation + line;
                }
                // System.out.println(translation);

                // read the "translatedText" field from the json
                String jsonString = translation;
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    JsonNode jsonNode = objectMapper.readTree(jsonString);
                    String translatedText = jsonNode.get("translatedText").asText();

                    // strip off quotation marks
                    String result = translatedText.substring(1, translatedText.length() - 1);
                    return result;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // if json fails to parse, return whole json String
                return translation;

            } catch (IOException e) {
                e.printStackTrace();
            }
           
        }
        // Return error if given improper arguments
        return new EvalError(EvalErrorMessage.expects_one_string(ControlFunctionRegistry.getFunctionName(this)));
    }

    @Override
    public String getDescription() {
        return "Translates the given text to english.";
    }

    @Override
    public String getParams() {
        return "string s1";
    }

    @Override
    public String getReturns() {
        return "string ret";
    }
}
