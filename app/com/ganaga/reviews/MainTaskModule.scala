package com.ganaga.reviews

import play.api.inject._
import play.api.inject.SimpleModule

class MainTaskModule extends SimpleModule(bind[MainTask].toSelf.eagerly())
