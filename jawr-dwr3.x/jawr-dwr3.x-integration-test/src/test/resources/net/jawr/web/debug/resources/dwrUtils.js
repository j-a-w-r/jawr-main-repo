/*
 * Copyright 2005 Joe Walker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 



















if (typeof dwr == 'undefined') dwr = {};
if (!dwr.util) dwr.util = {};


dwr.util._escapeHtml = true;




dwr.util.setEscapeHtml = function(escapeHtml) {
dwr.util._escapeHtml = escapeHtml;
};


dwr.util._shouldEscapeHtml = function(options) {
if (options && options.escapeHtml != null) {
return options.escapeHtml;
}
return dwr.util._escapeHtml;
};





dwr.util.escapeHtml = function(original) {
return original.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;').replace(/'/g,'&#039;');
};





dwr.util.unescapeHtml = function(original) {
return original.replace(/&lt;/g,'<').replace(/&gt;/g,'>').replace(/&quot;/g,'"').replace(/&#039;/g,"'").replace(/&amp;/g,'&');
};





dwr.util.replaceXmlCharacters = function(original) {
original = original.replace("&", "+");
original = original.replace("<", "\u2039");
original = original.replace(">", "\u203A");
original = original.replace("\'", "\u2018");
original = original.replace("\"", "\u201C");
return original;
};





dwr.util.containsXssRiskyCharacters = function(original) {
return (original.indexOf('&') != -1
|| original.indexOf('<') != -1
|| original.indexOf('>') != -1
|| original.indexOf('\'') != -1
|| original.indexOf('\"') != -1);
};




dwr.util.onReturn = function(event, action) {
if (!event) event = window.event;
if (event && event.keyCode && event.keyCode == 13) action();
};




dwr.util.selectRange = function(ele, start, end) {
ele = dwr.util._getElementById(ele, "selectRange()");
if (ele == null) return;
if (ele.setSelectionRange) {
ele.setSelectionRange(start, end);
}
else if (ele.createTextRange) {
var range = ele.createTextRange();
range.moveStart("character", start);
range.moveEnd("character", end - ele.value.length);
range.select();
}
ele.focus();
};




dwr.util.byId = function() {
var elems = [];
for (var i = 0; i < arguments.length; i++) {
var idOrElem = arguments[i];
var elem;
if (typeof idOrElem == 'string') {
var elem = document.getElementById(idOrElem);

if (document.all && elem && dwr.util._getId(elem) != idOrElem) {
elem = null;
var maybeElems = document.all[idOrElem];
if (maybeElems.tagName) maybeElems = [maybeElems];
for (var j = 0; j < maybeElems.length; j++) {
if (dwr.util._getId(maybeElems[j]) == idOrElem) {
elem = maybeElems[j];
break;
}
}
}
}
else {
elem = idOrElem;
}
if (arguments.length == 1) {
return elem;
}
elems.push(elem);
}
return elems;
}

dwr.util._getId = function(elem) {





var elemId = elem.getAttribute("id");
if (dwr.util._isObject(elemId)) {
elemId = elem.attributes.id.value;
}
return elemId;
};




if (typeof $ == 'undefined') {
$ = dwr.util.byId;
}




dwr.util.toDescriptiveString = function(data, showLevels, options) {
if (showLevels === undefined) showLevels = 1;
var opt = {};
if (dwr.util._isObject(options)) opt = options;
var defaultoptions = {
escapeHtml:false,
baseIndent: "",
childIndent: "\u00A0\u00A0",
lineTerminator: "\n",
oneLineMaxItems: 5,
shortStringMaxLength: 13,
propertyNameMaxLength: 30
};
for (var p in defaultoptions) {
if (!(p in opt)) {
opt[p] = defaultoptions[p];
}
}

var skipDomProperties = {
document:true, ownerDocument:true,
all:true,
parentElement:true, parentNode:true, offsetParent:true,
children:true, firstChild:true, lastChild:true,
previousSibling:true, nextSibling:true,
innerHTML:true, outerHTML:true,
innerText:true, outerText:true, textContent:true,
attributes:true,
style:true, currentStyle:true, runtimeStyle:true,
parentTextEdit:true
};

function recursive(data, showLevels, indentDepth, options) {
var reply = "";
try {

if (dwr.util._isString(data)) {
var str = data;
if (showLevels == 0 && str.length > options.shortStringMaxLength)
str = str.substring(0, options.shortStringMaxLength-3) + "...";
if (options.escapeHtml) {


var lines = str.split("\n");
for (var i = 0; i < lines.length; i++) lines[i] = dwr.util.escapeHtml(lines[i]);
str = lines.join("\n");
}
if (showLevels == 0) {
str = str.replace(/\n|\r|\t/g, function(ch) {
switch (ch) {
case "\n": return "\\n";
case "\r": return "";
case "\t": return "\\t";
}
});
}
else {
str = str.replace(/\n|\r|\t/g, function(ch) {
switch (ch) {
case "\n": return options.lineTerminator + indent(indentDepth+1, options);
case "\r": return "";
case "\t": return "\\t";
}
});
}
reply = '"' + str + '"';
}


else if (dwr.util._isFunction(data)) {
reply = "function";
}


else if (dwr.util._isArrayLike(data)) {
if (showLevels == 0) {
if (data.length > 0)
reply = "[...]";
else
reply = "[]";
}
else {
var strarr = [];
strarr.push("[");
var count = 0;
for (var i = 0; i < data.length; i++) {
if (!(i in data) && data != "[object NodeList]") continue;
var itemvalue = data[i];
if (count > 0) strarr.push(", ");
if (showLevels == 1) {
if (count == options.oneLineMaxItems) {
strarr.push("...");
break;
}
}
else {
strarr.push(options.lineTerminator + indent(indentDepth+1, options));
}
if (i != count) {
strarr.push(i);
strarr.push(":");
}
strarr.push(recursive(itemvalue, showLevels-1, indentDepth+1, options));
count++;
}
if (showLevels > 1) strarr.push(options.lineTerminator + indent(indentDepth, options));
strarr.push("]");
reply = strarr.join("");
}
}


else if (dwr.util._isObject(data) && !dwr.util._isDate(data)) {
if (showLevels == 0) {
reply = dwr.util._detailedTypeOf(data);
}
else {
var strarr = [];
if (dwr.util._detailedTypeOf(data) != "Object") {
strarr.push(dwr.util._detailedTypeOf(data));
if (typeof data.valueOf() != "object") {
strarr.push(":");
strarr.push(recursive(data.valueOf(), 1, indentDepth, options));
}
strarr.push(" ");
}
strarr.push("{");
var isDomObject = dwr.util._isHTMLElement(data);
var count = 0;
for (var prop in data) {
var propvalue = data[prop];
if (isDomObject) {
if (propvalue == null) continue;
if (typeof propvalue == "function") continue;
if (skipDomProperties[prop]) continue;
if (prop.toUpperCase() == prop) continue;
}
if (count > 0) strarr.push(", ");
if (showLevels == 1) {
if (count == options.oneLineMaxItems) {
strarr.push("...");
break;
}
}
else {
strarr.push(options.lineTerminator + indent(indentDepth+1, options));
}
strarr.push(prop.length > options.propertyNameMaxLength ? prop.substring(0, options.propertyNameMaxLength-3) + "..." : prop);
strarr.push(":");
strarr.push(recursive(propvalue, showLevels-1, indentDepth+1, options));
count++;
}
if (showLevels > 1 && count > 0) strarr.push(options.lineTerminator + indent(indentDepth, options));
strarr.push("}");
reply = strarr.join("");
}
}


else {
reply = "" + data;
}

return reply;
}
catch(err) {
return (err.message ? err.message : ""+err);
}
}

function indent(count, options) {
var strarr = [];
strarr.push(options.baseIndent);
for (var i=0; i<count; i++) {
strarr.push(options.childIndent);
}
return strarr.join("");
};

return recursive(data, showLevels, 0, opt);
};




dwr.util.useLoadingMessage = function(message) {
var loadingMessage;
if (message) loadingMessage = message;
else loadingMessage = "Loading";
dwr.engine.setPreHook(function() {
var disabledZone = dwr.util.byId('disabledZone');
if (!disabledZone) {
disabledZone = document.createElement('div');
disabledZone.setAttribute('id', 'disabledZone');
disabledZone.style.position = "absolute";
disabledZone.style.zIndex = "1000";
disabledZone.style.left = "0px";
disabledZone.style.top = "0px";
disabledZone.style.width = "100%";
disabledZone.style.height = "100%";

if (window.ActiveXObject) {
disabledZone.style.background = "white";
disabledZone.style.filter = "alpha(opacity=0)";
}
document.body.appendChild(disabledZone);
var messageZone = document.createElement('div');
messageZone.setAttribute('id', 'messageZone');
messageZone.style.position = "absolute";
messageZone.style.top = "0px";
messageZone.style.right = "0px";
messageZone.style.background = "red";
messageZone.style.color = "white";
messageZone.style.fontFamily = "Arial,Helvetica,sans-serif";
messageZone.style.padding = "4px";
document.body.appendChild(messageZone);
var text = document.createTextNode(loadingMessage);
messageZone.appendChild(text);
dwr.util._disabledZoneUseCount = 1;
}
else {
dwr.util.byId('messageZone').innerHTML = loadingMessage;
disabledZone.style.visibility = 'visible';
dwr.util._disabledZoneUseCount++;
dwr.util.byId('messageZone').style.visibility = 'visible';
}
});
dwr.engine.setPostHook(function() {
dwr.util._disabledZoneUseCount--;
if (dwr.util._disabledZoneUseCount == 0) {
dwr.util.byId('disabledZone').style.visibility = 'hidden';
dwr.util.byId('messageZone').style.visibility = 'hidden';
}
});
};




dwr.util.setHighlightHandler = function(handler) {
dwr.util._highlightHandler = handler;
};




dwr.util.yellowFadeHighlightHandler = function(ele) {
dwr.util._yellowFadeProcess(ele, 0);
};
dwr.util._yellowFadeSteps = [ "d0", "b0", "a0", "90", "98", "a0", "a8", "b0", "b8", "c0", "c8", "d0", "d8", "e0", "e8", "f0", "f8" ];
dwr.util._yellowFadeProcess = function(ele, colorIndex) {
ele = dwr.util.byId(ele);
if (colorIndex < dwr.util._yellowFadeSteps.length) {
ele.style.backgroundColor = "#ffff" + dwr.util._yellowFadeSteps[colorIndex];
setTimeout("dwr.util._yellowFadeProcess('" + dwr.util._getId(ele) + "'," + (colorIndex + 1) + ")", 200);
}
else {
ele.style.backgroundColor = "transparent";
}
};




dwr.util.borderFadeHighlightHandler = function(ele) {
ele.style.borderWidth = "2px";
ele.style.borderStyle = "solid";
dwr.util._borderFadeProcess(ele, 0);
};
dwr.util._borderFadeSteps = [ "d0", "b0", "a0", "90", "98", "a0", "a8", "b0", "b8", "c0", "c8", "d0", "d8", "e0", "e8", "f0", "f8" ];
dwr.util._borderFadeProcess = function(ele, colorIndex) {
ele = dwr.util.byId(ele);
if (colorIndex < dwr.util._borderFadeSteps.length) {
ele.style.borderColor = "#ff" + dwr.util._borderFadeSteps[colorIndex] + dwr.util._borderFadeSteps[colorIndex];
setTimeout("dwr.util._borderFadeProcess('" + dwr.util._getId(ele) + "'," + (colorIndex + 1) + ")", 200);
}
else {
ele.style.backgroundColor = "transparent";
}
};




dwr.util.focusHighlightHandler = function(ele) {
try {
ele.focus();
}
catch (ex) {   }
};


dwr.util._highlightHandler = null;




dwr.util.highlight = function(ele, options) {
if (options && options.highlightHandler) {
options.highlightHandler(dwr.util.byId(ele));
}
else if (dwr.util._highlightHandler != null) {
dwr.util._highlightHandler(dwr.util.byId(ele));
}
};




dwr.util.setValue = function(ele, val, options) {
if (val == null) val = "";
if (options == null) options = {};

var orig = ele;
ele = dwr.util.byId(ele);
var nodes = null;
if (ele == null) {

nodes = document.getElementsByName(orig);
if (nodes.length >= 1) ele = nodes.item(0);
}

if (ele == null) {
dwr.util._debug("setValue() can't find an element with id/name: " + orig + ".");
return;
}


dwr.util.highlight(ele, options);

if (dwr.util._isHTMLElement(ele, "select")) {
if (ele.type == "select-multiple" && dwr.util._isArray(val)) dwr.util._selectListItems(ele, val);
else dwr.util._selectListItem(ele, val);
return;
}

if (dwr.util._isHTMLElement(ele, "input")) {
if (ele.type == "radio" || ele.type == "checkbox") {
if (nodes && nodes.length >= 1) {
for (var i = 0; i < nodes.length; i++) {
var node = nodes.item(i);
if (node.type != ele.type) continue;
if (dwr.util._isArray(val)) {
node.checked = false;
for (var j = 0; j < val.length; j++)
if (val[j] == node.value) node.checked = true;
}
else {
node.checked = (node.value == val);
}
}
}
else {
ele.checked = (val == true);
}
}
else ele.value = val;

return;
}

if (dwr.util._isHTMLElement(ele, "textarea")) {
ele.value = val;
return;
}

if (dwr.util._isHTMLElement(ele, "img")) {
ele.src = val;
return;
}



if (val.nodeType) {
if (val.nodeType == 9  ) val = val.documentElement;
val = dwr.util._importNode(ele.ownerDocument, val, true);
ele.appendChild(val);
return;
}


if (dwr.util._shouldEscapeHtml(options)) {
if ("textContent" in ele) ele.textContent = val.toString();
else if ("innerText" in ele) ele.innerText = val.toString();
else ele.innerHTML = dwr.util.escapeHtml(val.toString());
}
else {
ele.innerHTML = val;
}
};






dwr.util._selectListItems = function(ele, val) {


var found  = 0;
var i;
var j;
for (i = 0; i < ele.options.length; i++) {
ele.options[i].selected = false;
for (j = 0; j < val.length; j++) {
if (ele.options[i].value == val[j]) {
ele.options[i].selected = true;
found++;
}
}
}

if (found == val.length) return;

for (i = 0; i < ele.options.length; i++) {
for (j = 0; j < val.length; j++) {
if (ele.options[i].text == val[j]) {
ele.options[i].selected = true;
}
}
}
};






dwr.util._selectListItem = function(ele, val) {


var found = false;
var i;
for (i = 0; i < ele.options.length; i++) {
if (ele.options[i].value == val) {
ele.options[i].selected = true;
found = true;
}
else {
ele.options[i].selected = false;
}
}


if (found) return;

for (i = 0; i < ele.options.length; i++) {
ele.options[i].selected = (ele.options[i].text == val);
}
};




dwr.util.getValue = function(ele, options) {
if (options == null) options = {};
var orig = ele;
ele = dwr.util.byId(ele);
var nodes = null;
if (ele == null) {

nodes = document.getElementsByName(orig);
if (nodes.length >= 1) ele = nodes.item(0);
}
if (ele == null) {
dwr.util._debug("getValue() can't find an element with id/name: " + orig + ".");
return "";
}

if (dwr.util._isHTMLElement(ele, "select")) {


if (ele.type == "select-multiple") {
var reply = new Array();
for (var i = 0; i < ele.options.length; i++) {
var item = ele.options[i];
if (item.selected) {
var valueAttr = item.getAttributeNode("value");
if (valueAttr && valueAttr.specified) {
reply.push(item.value);
}
else {
reply.push(item.text);
}
}
}
return reply;
}
else {
var sel = ele.selectedIndex;
if (sel != -1) {
var item = ele.options[sel];
var valueAttr = item.getAttributeNode("value");
if (valueAttr && valueAttr.specified) {
return item.value;
}
return item.text;
}
else {
return "";
}
}
}

if (dwr.util._isHTMLElement(ele, "input")) {
if (ele.type == "radio") {
if (nodes && nodes.length >= 1) {
for (var i = 0; i < nodes.length; i++) {
var node = nodes.item(i);
if (node.type == ele.type) {
if (node.checked) return node.value;
}
}
}
return ele.checked;
}
if (ele.type == "checkbox") {
if (nodes && nodes.length >= 1) {
var reply = [];
for (var i = 0; i < nodes.length; i++) {
var node = nodes.item(i);
if (node.type == ele.type) {
if (node.checked) reply.push(node.value);
}
}
return reply;
}
return ele.checked;
}
if (ele.type == "file") {
return ele;
}
return ele.value;
}

if (dwr.util._isHTMLElement(ele, "textarea")) {
return ele.value;
}

if (dwr.util._shouldEscapeHtml(options)) {
if ("textContent" in ele) return ele.textContent;
else if ("innerText" in ele) return ele.innerText;
}
return ele.innerHTML;
};




dwr.util.getText = function(ele) {
ele = dwr.util._getElementById(ele, "getText()");
if (ele == null) return null;
if (!dwr.util._isHTMLElement(ele, "select")) {
dwr.util._debug("getText() can only be used with select elements. Attempt to use: " + dwr.util._detailedTypeOf(ele) + " from  id: " + orig + ".");
return "";
}



var sel = ele.selectedIndex;
if (sel != -1) {
return ele.options[sel].text;
}
else {
return "";
}
};






dwr.util.setValues = function(data, options) {
var prefix = "";
var depth = 100;
if (options && "prefix" in options) prefix = options.prefix;
if (options && "idPrefix" in options) prefix = options.idPrefix;
if (options && "depth" in options) depth = options.depth;
dwr.util._setValuesRecursive(data, prefix, depth, options);
};




dwr.util._setValuesRecursive = function(data, idpath, depth, options) {
if (depth == 0) return;


if (dwr.util._isArray(data) && data.length > 0 && dwr.util._isObject(data[0])) {
for (var i = 0; i < data.length; i++) {
dwr.util._setValuesRecursive(data[i], idpath+"["+i+"]", depth-1, options);
}
}

else if (dwr.util._isObject(data) && !dwr.util._isArray(data)) {
for (var prop in data) {
var subidpath = idpath ? idpath+"."+prop : prop;

if (dwr.util._isObject(data[prop]) && !dwr.util._isArray(data[prop]) && !dwr.util._isDate(data[prop])
|| dwr.util._isArray(data[prop]) && data[prop].length > 0 && dwr.util._isObject(data[prop][0])) {
dwr.util._setValuesRecursive(data[prop], subidpath, depth-1, options);
}

else if (typeof data[prop] == "function") {

}


else {

if (dwr.util.byId(subidpath) != null || document.getElementsByName(subidpath).length >= 1) {
dwr.util.setValue(subidpath, data[prop], options);
}
}
}
}
};








dwr.util.getValues = function(data, options) {
if (typeof data == "string" || dwr.util._isHTMLElement(data)) {
return dwr.util.getFormValues(data);
}
else {
var prefix = "";
var depth = 100;
if (options != null && "prefix" in options) prefix = options.prefix;
if (options != null && "idPrefix" in options) prefix = options.idPrefix;
if (options != null && "depth" in options) depth = options.depth;
dwr.util._getValuesRecursive(data, prefix, depth, options);
return data;
}
};





dwr.util.getFormValues = function(eleOrNameOrId) {
var ele = null;
if (typeof eleOrNameOrId == "string") {
ele = document.forms[eleOrNameOrId];
if (ele == null) ele = dwr.util.byId(eleOrNameOrId);
}
else if (dwr.util._isHTMLElement(eleOrNameOrId)) {
ele = eleOrNameOrId;
}
if (ele != null) {
if (ele.elements == null) {
alert("getFormValues() requires an object or reference to a form element.");
return null;
}
var reply = {};
var name;
var value;
for (var i = 0; i < ele.elements.length; i++) {
if (ele[i].type in {button:0,submit:0,reset:0,image:0,file:0}) continue;
if (ele[i].name) {
name = ele[i].name;
value = dwr.util.getValue(name);
}
else {
if (ele[i].id) name = ele[i].id;
else name = "element" + i;
value = dwr.util.getValue(ele[i]);
}
reply[name] = value;
}
return reply;
}
};




dwr.util._getValuesRecursive = function(data, idpath, depth, options) {
if (depth == 0) return;


if (dwr.util._isArray(data) && data.length > 0 && dwr.util._isObject(data[0])) {
for (var i = 0; i < data.length; i++) {
dwr.util._getValuesRecursive(data[i], idpath+"["+i+"]", depth-1, options);
}
}

else if (dwr.util._isObject(data) && !dwr.util._isArray(data)) {
for (var prop in data) {
var subidpath = idpath ? idpath+"."+prop : prop;

if (dwr.util._isObject(data[prop]) && !dwr.util._isArray(data[prop])
|| dwr.util._isArray(data[prop]) && data[prop].length > 0 && dwr.util._isObject(data[prop][0])) {
dwr.util._getValuesRecursive(data[prop], subidpath, depth-1, options);
}

else if (typeof data[prop] == "function") {

}


else {

if (dwr.util.byId(subidpath) != null || document.getElementsByName(subidpath).length >= 1) {
data[prop] = dwr.util.getValue(subidpath);
}
}
}
}
};




dwr.util.addOptions = function(ele, data ) {
ele = dwr.util._getElementById(ele, "addOptions()");
if (ele == null) return;
var useOptions = dwr.util._isHTMLElement(ele, "select");
var useLi = dwr.util._isHTMLElement(ele, ["ul", "ol"]);
if (!useOptions && !useLi) {
dwr.util._debug("addOptions() can only be used with select/ul/ol elements. Attempt to use: " + dwr.util._detailedTypeOf(ele));
return;
}
if (data == null) return;

var argcount = arguments.length;
var options = {};
var lastarg = arguments[argcount - 1];
if (argcount > 2 && dwr.util._isObject(lastarg)) {
options = lastarg;
argcount--;
}
var arg3 = null; if (argcount >= 3) arg3 = arguments[2];
var arg4 = null; if (argcount >= 4) arg4 = arguments[3];
if (!options.optionCreator && useOptions) options.optionCreator = dwr.util._defaultOptionCreator;
if (!options.optionCreator && useLi) options.optionCreator = dwr.util._defaultListItemCreator;
options.document = ele.ownerDocument;

var text, value, li;
if (dwr.util._isArray(data)) {

for (var i = 0; i < data.length; i++) {
options.data = data[i];
options.text = null;
options.value = null;
if (useOptions) {
if (arg3 != null) {
if (arg4 != null) {
options.text = dwr.util._getValueFrom(data[i], arg4);
options.value = dwr.util._getValueFrom(data[i], arg3);
}
else options.text = options.value = dwr.util._getValueFrom(data[i], arg3);
}
else options.text = options.value = dwr.util._getValueFrom(data[i]);

if (options.text != null || options.value) {
var opt = options.optionCreator(options);
opt.text = options.text;
opt.value = options.value;
ele.options[ele.options.length] = opt;
}
}
else {
options.value = dwr.util._getValueFrom(data[i], arg3);
if (options.value != null) {
li = options.optionCreator(options);
if (dwr.util._shouldEscapeHtml(options)) {
options.value = dwr.util.escapeHtml(options.value);
}
li.innerHTML = options.value;
ele.appendChild(li);
}
}
}
}
else if (arg4 != null) {
if (!useOptions) {
alert("dwr.util.addOptions can only create select lists from objects.");
return;
}
for (var prop in data) {
options.data = data[prop];
options.value = dwr.util._getValueFrom(data[prop], arg3);
options.text = dwr.util._getValueFrom(data[prop], arg4);

if (options.text != null || options.value) {
var opt = options.optionCreator(options);
opt.text = options.text;
opt.value = options.value;
ele.options[ele.options.length] = opt;
}
}
}
else {
if (!useOptions) {
dwr.util._debug("dwr.util.addOptions can only create select lists from objects.");
return;
}
for (var prop in data) {
if (typeof data[prop] == "function") continue;
options.data = data[prop];
if (arg3 == null) {
options.value = prop;
options.text = data[prop];
}
else {
options.value = data[prop];
options.text = prop;
}
if (options.text != null || options.value) {
var opt = options.optionCreator(options);
opt.text = options.text;
opt.value = options.value;
ele.options[ele.options.length] = opt;
}
}
}


dwr.util.highlight(ele, options);
};




dwr.util._getValueFrom = function(data, method) {
if (method == null) return data;
else if (typeof method == 'function') return method(data);
else return data[method];
};




dwr.util._defaultOptionCreator = function(options) {
return options.document.createElement("option");
};




dwr.util._defaultListItemCreator = function(options) {
return options.document.createElement("li");
};




dwr.util.removeAllOptions = function(ele) {
ele = dwr.util._getElementById(ele, "removeAllOptions()");
if (ele == null) return;
var useOptions = dwr.util._isHTMLElement(ele, "select");
var useLi = dwr.util._isHTMLElement(ele, ["ul", "ol"]);
if (!useOptions && !useLi) {
dwr.util._debug("removeAllOptions() can only be used with select, ol and ul elements. Attempt to use: " + dwr.util._detailedTypeOf(ele));
return;
}
if (useOptions) {
ele.options.length = 0;
}
else {
while (ele.childNodes.length > 0) {
ele.removeChild(ele.firstChild);
}
}
};




dwr.util.addRows = function(ele, data, cellFuncs, options) {
ele = dwr.util._getElementById(ele, "addRows()");
if (ele == null) return;
if (!dwr.util._isHTMLElement(ele, ["table", "tbody", "thead", "tfoot"])) {
dwr.util._debug("addRows() can only be used with table, tbody, thead and tfoot elements. Attempt to use: " + dwr.util._detailedTypeOf(ele));
return;
}
if (!options) options = {};
if (!options.rowCreator) options.rowCreator = dwr.util._defaultRowCreator;
if (!options.cellCreator) options.cellCreator = dwr.util._defaultCellCreator;
options.document = ele.ownerDocument;
var tr, rowNum;
if (dwr.util._isArray(data)) {
for (rowNum = 0; rowNum < data.length; rowNum++) {
options.rowData = data[rowNum];
options.rowIndex = rowNum;
options.rowNum = rowNum;
options.data = null;
options.cellNum = -1;
tr = dwr.util._addRowInner(cellFuncs, options);
if (tr != null) ele.appendChild(tr);
}
}
else if (typeof data == "object") {
rowNum = 0;
for (var rowIndex in data) {
options.rowData = data[rowIndex];
options.rowIndex = rowIndex;
options.rowNum = rowNum;
options.data = null;
options.cellNum = -1;
tr = dwr.util._addRowInner(cellFuncs, options);
if (tr != null) ele.appendChild(tr);
rowNum++;
}
}

dwr.util.highlight(ele, options);
};




dwr.util._emptyTableCellReplacement = "<div style='width:0;height:0;overflow:hidden;'></div>";




dwr.util._addRowInner = function(cellFuncs, options) {
var tr = options.rowCreator(options);
if (tr == null) return null;
for (var cellNum = 0; cellNum < cellFuncs.length; cellNum++) {
var func = cellFuncs[cellNum];
if (typeof func == 'function') options.data = func(options.rowData, options);
else options.data = func || "";
options.cellNum = cellNum;
var td = options.cellCreator(options);
if (td != null) {
if ("data" in options) {
if (dwr.util._isHTMLElement(options.data)) td.appendChild(options.data);
else {
if (dwr.util._shouldEscapeHtml(options) && typeof(options.data) == "string") {
td.innerHTML = dwr.util.escapeHtml(options.data);
}
else {
td.innerHTML = options.data;
}
}
}
else {
td.innerHTML = dwr.util._emptyTableCellReplacement;
}
tr.appendChild(td);
}
}
return tr;
};




dwr.util._defaultRowCreator = function(options) {
return options.document.createElement("tr");
};




dwr.util._defaultCellCreator = function(options) {
return options.document.createElement("td");
};




dwr.util.removeAllRows = function(ele, options) {
ele = dwr.util._getElementById(ele, "removeAllRows()");
if (ele == null) return;
if (!options) options = {};
if (!options.filter) options.filter = function() { return true; };
if (!dwr.util._isHTMLElement(ele, ["table", "tbody", "thead", "tfoot"])) {
dwr.util._debug("removeAllRows() can only be used with table, tbody, thead and tfoot elements. Attempt to use: " + dwr.util._detailedTypeOf(ele));
return;
}
var child = ele.firstChild;
var next;
while (child != null) {
next = child.nextSibling;
if (options.filter(child)) {
ele.removeChild(child);
}
child = next;
}
};




dwr.util.setClassName = function(ele, className) {
ele = dwr.util._getElementById(ele, "setClassName()");
if (ele == null) return;
ele.className = className;
};




dwr.util.addClassName = function(ele, className) {
ele = dwr.util._getElementById(ele, "addClassName()");
if (ele == null) return;
ele.className += " " + className;
};





dwr.util.removeClassName = function(ele, className) {
ele = dwr.util._getElementById(ele, "removeClassName()");
if (ele == null) return;
var regex = new RegExp("(^|\\s)" + className + "(\\s|$)", 'g');
ele.className = ele.className.replace(regex, '');
};




dwr.util.toggleClassName = function(ele, className) {
ele = dwr.util._getElementById(ele, "toggleClassName()");
if (ele == null) return;
var regex = new RegExp("(^|\\s)" + className + "(\\s|$)");
if (regex.test(ele.className)) {
ele.className = ele.className.replace(regex, '');
}
else {
ele.className += " " + className;
}
};




dwr.util.cloneNode = function(ele, options) {
ele = dwr.util._getElementById(ele, "cloneNode()");
if (ele == null) return null;
if (options == null) options = {};
var clone = ele.cloneNode(true);
if ("idPrefix" in options || "idSuffix" in options) {
dwr.util._updateIds(clone, options);
}
else {
dwr.util._removeIds(clone);
}
ele.parentNode.insertBefore(clone, ele);
return clone;
};




dwr.util._updateIds = function(ele, options) {
if (options == null) options = {};
if (dwr.util._getId(ele)) {
ele.setAttribute("id",
("idPrefix" in options ? options.idPrefix : "")
+ dwr.util._getId(ele)
+ ("idSuffix" in options ? options.idSuffix : ""));
}
var children = ele.childNodes;
for (var i = 0; i < children.length; i++) {
var child = children.item(i);
if (child.nodeType == 1  ) {
dwr.util._updateIds(child, options);
}
}
};




dwr.util._removeIds = function(ele) {
if (dwr.util._getId(ele)) ele.removeAttribute("id");
var children = ele.childNodes;
for (var i = 0; i < children.length; i++) {
var child = children.item(i);
if (child.nodeType == 1  ) {
dwr.util._removeIds(child);
}
}
};





dwr.util.cloneNodeForValues = function(templateEle, data, options) {
templateEle = dwr.util._getElementById(templateEle, "cloneNodeForValues()");
if (templateEle == null) return null;
if (options == null) options = {};
var idpath;
if (options.idPrefix != null)
idpath = options.idPrefix;
else
idpath = dwr.util._getId(templateEle) || "";
return dwr.util._cloneNodeForValuesRecursive(templateEle, data, idpath, options);
};




dwr.util._cloneNodeForValuesRecursive = function(templateEle, data, idpath, options) {


if (dwr.util._isArray(data)) {
var clones = [];
for (var i = 0; i < data.length; i++) {
var item = data[i];
var clone = dwr.util._cloneNodeForValuesRecursive(templateEle, item, idpath + "[" + i + "]", options);
clones.push(clone);
}
return clones;
}
else



if (dwr.util._isObject(data) && !dwr.util._isArray(data)) {
var clone = templateEle.cloneNode(true);
if (options.updateCloneStyle && clone.style) {
for (var propname in options.updateCloneStyle) {
clone.style[propname] = options.updateCloneStyle[propname];
}
}
dwr.util._replaceIds(clone, dwr.util._getId(templateEle), idpath);
templateEle.parentNode.insertBefore(clone, templateEle);
dwr.util._cloneSubArrays(data, idpath, options);
return clone;
}


return null;
};





dwr.util._replaceIds = function(ele, oldidpath, newidpath) {
var currId = dwr.util._getId(ele);
if (currId) {
var newId = null;
if (currId == oldidpath) {
newId = newidpath;
}
else if (currId.length > oldidpath.length) {
if (currId.substr(0, oldidpath.length) == oldidpath) {
var trailingChar = currId.charAt(oldidpath.length);
if (trailingChar == "." || trailingChar == "[") {
newId = newidpath + currId.substr(oldidpath.length);
}
}
}
if (newId) {
ele.setAttribute("id", newId);
}
else {
ele.removeAttribute("id");
}
}
var children = ele.childNodes;
for (var i = 0; i < children.length; i++) {
var child = children.item(i);
if (child.nodeType == 1  ) {
dwr.util._replaceIds(child, oldidpath, newidpath);
}
}
};





dwr.util._cloneSubArrays = function(data, idpath, options) {
for (prop in data) {
var value = data[prop];

if (dwr.util._isArray(value)) {

if (value.length > 0 && dwr.util._isObject(value[0])) {
var subTemplateId = idpath + "." + prop;
var subTemplateEle = dwr.util.byId(subTemplateId);
if (subTemplateEle != null) {
dwr.util._cloneNodeForValuesRecursive(subTemplateEle, value, subTemplateId, options);
}
}
}

else if (dwr.util._isObject(value)) {
dwr.util._cloneSubArrays(value, idpath + "." + prop, options);
}
}
};




dwr.util._getElementById = function(ele, source) {
var orig = ele;
ele = dwr.util.byId(ele);
if (ele == null) {
dwr.util._debug(source + " can't find an element with id: " + orig + ".");
}
return ele;
};







dwr.util._isHTMLElement = function(ele, nodeName) {
if (ele == null || typeof ele != "object" || ele.nodeName == null) {
return false;
}
if (nodeName != null) {
var test = ele.nodeName.toLowerCase();
if (typeof nodeName == "string") {
return test == nodeName.toLowerCase();
}
if (dwr.util._isArray(nodeName)) {
var match = false;
for (var i = 0; i < nodeName.length && !match; i++) {
if (test == nodeName[i].toLowerCase()) {
match =  true;
}
}
return match;
}
dwr.util._debug("dwr.util._isHTMLElement was passed test node name that is neither a string or array of strings");
return false;
}
return true;
};




dwr.util._detailedTypeOf = function(x) {
var reply = typeof x;
if (reply == "object") {
reply = Object.prototype.toString.apply(x);
reply = reply.substring(8, reply.length-1);
}
return reply;
};




dwr.util._isObject = function(data) {
return (data && typeof data == "object");
};




dwr.util._isArray = function(data) {
return (data && Object.prototype.toString.call(data)=="[object Array]");
};




dwr.util._isArrayLike = function(data) {
return data
&& (typeof data.length == "number")
&& ((data.propertyIsEnumerable && data.propertyIsEnumerable("length")==false) || !data.constructor || data!="[object Object]")
&& !dwr.util._isString(data)
&& !dwr.util._isFunction(data)
&& !data.tagName;
};




dwr.util._isString = function(data) {
return (data && (typeof data == "string" || Object.prototype.toString.call(data) == "[object String]"));
};




dwr.util._isFunction = function(data) {
return (data && (typeof data == "function" || Object.prototype.toString.call(data) == "[object Function]")
&& data != "[object NodeList]");
};




dwr.util._isDate = function(data) {
return (data && Object.prototype.toString.call(data)=="[object Date]");
};




dwr.util._importNode = function(doc, importedNode, deep) {
var newNode;

if (importedNode.nodeType == 1  ) {
newNode = doc.createElement(importedNode.nodeName);

for (var i = 0; i < importedNode.attributes.length; i++) {
var attr = importedNode.attributes[i];
if (attr.nodeValue != null && attr.nodeValue != '') {
newNode.setAttribute(attr.name, attr.nodeValue);
}
}

if (importedNode.style != null) {
newNode.style.cssText = importedNode.style.cssText;
}
}
else if (importedNode.nodeType == 3  ) {
newNode = doc.createTextNode(importedNode.nodeValue);
}

if (deep && importedNode.hasChildNodes()) {
for (i = 0; i < importedNode.childNodes.length; i++) {
newNode.appendChild(dwr.util._importNode(doc, importedNode.childNodes[i], true));
}
}

return newNode;
};


dwr.util._debug = function(message, stacktrace) {
var written = false;
try {
if (window.console) {
if (stacktrace && window.console.trace) window.console.trace();
window.console.log(message);
written = true;
}
else if (window.opera && window.opera.postError) {
window.opera.postError(message);
written = true;
}
}
catch (ex) {   }

if (!written) {
var debug = document.getElementById("dwr-debug");
if (debug) {
var contents = message + "<br/>" + debug.innerHTML;
if (contents.length > 2048) contents = contents.substring(0, 2048);
debug.innerHTML = contents;
}
}
};
